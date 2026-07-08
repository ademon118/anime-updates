package com.aura.anime_updates.features.watchparty.domain.service;

import com.aura.anime_updates.features.friends.domain.exceptions.FriendException;
import com.aura.anime_updates.features.friends.domain.service.FriendService;
import com.aura.anime_updates.features.notification.NotificationService;
import com.aura.anime_updates.features.notification.payloadBuilders.WatchPartyNotificationPayloadBuilder;
import com.aura.anime_updates.features.watchparty.api.response.PartyInviteResponse;
import com.aura.anime_updates.features.watchparty.api.response.PartyStateResponse;
import com.aura.anime_updates.features.watchparty.domain.entity.PendingInvite;
import com.aura.anime_updates.features.watchparty.domain.entity.WatchParty;
import com.aura.anime_updates.features.watchparty.domain.exceptions.WatchPartyException;
import com.aura.anime_updates.features.user.domain.entity.User;
import com.aura.anime_updates.features.user.domain.repository.UserRepository;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WatchPartyService {

    private final WatchPartyManager manager;
    private final FriendService friendService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final WatchPartyNotificationPayloadBuilder notificationPayloadBuilder;
    private final CleanupService cleanupService;
    private final WatchPartySyncPublisher syncPublisher;

    public PartyInviteResponse inviteFriend(String leaderUsername, String friendUsername) {
        String normalizedLeaderUsername = requireUsername(leaderUsername);
        String normalizedFriendUsername = requireUsername(friendUsername);

        if (normalizedLeaderUsername.equalsIgnoreCase(normalizedFriendUsername)) {
            throw WatchPartyException.selfInvite();
        }
        if (!friendService.areFriends(normalizedLeaderUsername, normalizedFriendUsername)) {
            throw WatchPartyException.notFriends();
        }

        User leader = userRepository.findByUserName(normalizedLeaderUsername)
                .orElseThrow(() -> FriendException.userNotFound(normalizedLeaderUsername));
        User friend = userRepository.findByUserName(normalizedFriendUsername)
                .orElseThrow(() -> FriendException.userNotFound(normalizedFriendUsername));

        synchronized (manager.leaderInviteLock(normalizedLeaderUsername)) {
            WatchParty party = manager.findActivePartyForLeader(normalizedLeaderUsername)
                    .orElseGet(() -> {
                        String partyId = UUID.randomUUID().toString();
                        return manager.createParty(normalizedLeaderUsername, partyId);
                    });

            if (party.getJoinedMembers().contains(normalizedFriendUsername)) {
                throw WatchPartyException.alreadyMember();
            }

            replacePendingInviteIfPresent(party, normalizedFriendUsername);

            String inviteToken = UUID.randomUUID().toString();
            long expiresAt = System.currentTimeMillis() + (CleanupService.INVITE_TTL_SECONDS * 1000);
            party.getPendingInvites().put(inviteToken, new PendingInvite(normalizedFriendUsername, expiresAt));
            cleanupService.scheduleInviteExpiry(party.getPartyId(), inviteToken);

            sendInviteNotification(leader, friend, party.getPartyId(), inviteToken);

            return PartyInviteResponse.builder()
                    .partyId(party.getPartyId())
                    .inviteToken(inviteToken)
                    .build();
        }
    }

    private void replacePendingInviteIfPresent(WatchParty party, String inviteeUsername) {
        String partyId = party.getPartyId();
        party.getPendingInvites().entrySet().removeIf(entry -> {
            if (!inviteeUsername.equals(entry.getValue().inviteeUsername())) {
                return false;
            }
            cleanupService.cancelInviteExpiry(partyId, entry.getKey());
            return true;
        });
    }

    private void sendInviteNotification(
            User leader,
            User friend,
            String partyId,
            String inviteToken
    ) {
        String leaderUsername = requireUsername(leader);
        Notification notification = notificationPayloadBuilder.buildInviteNotification(leaderUsername);
        var data = notificationPayloadBuilder.buildInviteData(partyId, inviteToken, leaderUsername);
        notificationService.sendNotificationToAllDevicesOfUsers(List.of(friend), notification, data);
    }

    public void acceptInvite(String joinerUsername, String partyId, String token) {
        String normalizedJoinerUsername = requireUsername(joinerUsername);

        WatchParty party = manager.getParty(partyId).orElseThrow(WatchPartyException::partyNotFound);
        PendingInvite invite = party.getPendingInvites().remove(token);

        if (invite == null || invite.isExpired()) {
            throw WatchPartyException.invalidToken();
        }
        if (!invite.inviteeUsername().equals(normalizedJoinerUsername)) {
            throw WatchPartyException.notInvitee();
        }
        if (!friendService.areFriends(party.getLeaderUsername(), normalizedJoinerUsername)) {
            throw WatchPartyException.notFriends();
        }

        cleanupService.cancelInviteExpiry(partyId, token);
        party.addMember(normalizedJoinerUsername);
        syncPublisher.publishMemberJoined(party, normalizedJoinerUsername);
    }

    public void declineInvite(String friendUsername, String partyId, String token) {
        String normalizedFriendUsername = requireUsername(friendUsername);

        WatchParty party = manager.getParty(partyId).orElseThrow(WatchPartyException::partyNotFound);
        PendingInvite invite = party.getPendingInvites().remove(token);

        if (invite == null || invite.isExpired()) {
            throw WatchPartyException.invalidToken();
        }
        if (!invite.inviteeUsername().equals(normalizedFriendUsername)) {
            throw WatchPartyException.notInvitee();
        }

        cleanupService.cancelInviteExpiry(partyId, token);

        User leader = userRepository.findByUserName(party.getLeaderUsername())
                .orElseThrow(() -> FriendException.userNotFound(party.getLeaderUsername()));

        Notification notification = notificationPayloadBuilder.buildDeclineNotification(normalizedFriendUsername);
        var data = notificationPayloadBuilder.buildDeclineData(partyId, normalizedFriendUsername);
        notificationService.sendNotificationToAllDevicesOfUsers(List.of(leader), notification, data);

        manager.cleanupIfAbandoned(partyId, party);
    }

    public PartyStateResponse getPartyState(String partyId, String requesterUsername) {
        String normalizedRequesterUsername = requireUsername(requesterUsername);

        WatchParty party = manager.getParty(partyId).orElseThrow(WatchPartyException::partyNotFound);

        if (!party.getJoinedMembers().contains(normalizedRequesterUsername)) {
            throw WatchPartyException.notMember();
        }

        return PartyStateResponse.builder()
                .partyId(party.getPartyId())
                .leaderUsername(party.getLeaderUsername())
                .videoUrl(party.getVideoUrl())
                .currentTimeStamp(party.getCurrentTimeStamp())
                .isPlaying(party.isPlaying())
                .members(party.getJoinedMembers())
                .activeMembers(party.getActiveMembers())
                .pendingInviteUsernames(resolvePendingInviteUsernames(party, normalizedRequesterUsername))
                .build();
    }

    private Set<String> resolvePendingInviteUsernames(WatchParty party, String requesterUsername) {
        if (!party.getLeaderUsername().equals(requesterUsername)) {
            return Set.of();
        }

        return party.getPendingInvites().values().stream()
                .filter(invite -> !invite.isExpired())
                .map(PendingInvite::inviteeUsername)
                .collect(Collectors.toSet());
    }

    private String requireUsername(User user) {
        String username = user.getUserName();
        if (!StringUtils.hasText(username)) {
            throw WatchPartyException.missingUsername();
        }
        return username.trim();
    }

    private String requireUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw WatchPartyException.missingUsername();
        }
        return username.trim();
    }
}
