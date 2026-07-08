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

    public PartyInviteResponse inviteFriend(Long leaderId, Long friendId) {
        if (leaderId.equals(friendId)) {
            throw WatchPartyException.selfInvite();
        }
        if (!friendService.areFriends(leaderId, friendId)) {
            throw WatchPartyException.notFriends();
        }

        User leader = userRepository.findById(leaderId)
                .orElseThrow(() -> FriendException.userNotFound(String.valueOf(leaderId)));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> FriendException.userNotFound(String.valueOf(friendId)));

        String leaderIdValue = String.valueOf(leaderId);
        String friendIdValue = String.valueOf(friendId);

        synchronized (manager.leaderInviteLock(leaderIdValue)) {
            WatchParty party = manager.findActivePartyForLeader(leaderIdValue)
                    .orElseGet(() -> {
                        String partyId = UUID.randomUUID().toString();
                        return manager.createParty(leaderIdValue, partyId);
                    });

            if (party.getJoinedMembers().contains(friendIdValue)) {
                throw WatchPartyException.alreadyMember();
            }

            replacePendingInviteIfPresent(party, friendIdValue);

            String inviteToken = UUID.randomUUID().toString();
            long expiresAt = System.currentTimeMillis() + (CleanupService.INVITE_TTL_SECONDS * 1000);
            party.getPendingInvites().put(inviteToken, new PendingInvite(friendIdValue, expiresAt));
            cleanupService.scheduleInviteExpiry(party.getPartyId(), inviteToken);

            sendInviteNotification(leader, friend, party.getPartyId(), inviteToken);

            return PartyInviteResponse.builder()
                    .partyId(party.getPartyId())
                    .inviteToken(inviteToken)
                    .build();
        }
    }

    private void replacePendingInviteIfPresent(WatchParty party, String friendId) {
        String partyId = party.getPartyId();
        party.getPendingInvites().entrySet().removeIf(entry -> {
            if (!friendId.equals(entry.getValue().friendId())) {
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
        Notification notification = notificationPayloadBuilder.buildInviteNotification(leader.getUserName());
        var data = notificationPayloadBuilder.buildInviteData(
                partyId,
                inviteToken,
                String.valueOf(leader.getId()),
                leader.getUserName()
        );
        notificationService.sendNotificationToAllDevicesOfUsers(List.of(friend), notification, data);
    }

    public void acceptInvite(Long userId, String partyId, String token) {
        WatchParty party = manager.getParty(partyId).orElseThrow(WatchPartyException::partyNotFound);
        PendingInvite invite = party.getPendingInvites().remove(token);

        if (invite == null || invite.isExpired()) {
            throw WatchPartyException.invalidToken();
        }
        if (!invite.friendId().equals(String.valueOf(userId))) {
            throw WatchPartyException.notInvitee();
        }
        if (!friendService.areFriends(Long.parseLong(party.getLeaderId()), userId)) {
            throw WatchPartyException.notFriends();
        }

        cleanupService.cancelInviteExpiry(partyId, token);
        party.addMember(String.valueOf(userId));

        User joiner = userRepository.findById(userId)
                .orElseThrow(() -> FriendException.userNotFound(String.valueOf(userId)));
        syncPublisher.publishMemberJoined(party, joiner.getUserName());
    }

    public void declineInvite(Long userId, String partyId, String token) {
        WatchParty party = manager.getParty(partyId).orElseThrow(WatchPartyException::partyNotFound);
        PendingInvite invite = party.getPendingInvites().remove(token);

        if (invite == null || invite.isExpired()) {
            throw WatchPartyException.invalidToken();
        }
        if (!invite.friendId().equals(String.valueOf(userId))) {
            throw WatchPartyException.notInvitee();
        }

        cleanupService.cancelInviteExpiry(partyId, token);

        User friend = userRepository.findById(userId)
                .orElseThrow(() -> FriendException.userNotFound(String.valueOf(userId)));
        User leader = userRepository.findById(Long.parseLong(party.getLeaderId()))
                .orElseThrow(() -> FriendException.userNotFound(party.getLeaderId()));

        Notification notification = notificationPayloadBuilder.buildDeclineNotification(friend.getUserName());
        var data = notificationPayloadBuilder.buildDeclineData(
                partyId,
                String.valueOf(userId),
                friend.getUserName()
        );
        notificationService.sendNotificationToAllDevicesOfUsers(List.of(leader), notification, data);

        manager.cleanupIfAbandoned(partyId, party);
    }

    public PartyStateResponse getPartyState(String partyId, Long userId) {
        WatchParty party = manager.getParty(partyId).orElseThrow(WatchPartyException::partyNotFound);

        if (!party.getJoinedMembers().contains(String.valueOf(userId))) {
            throw WatchPartyException.notMember();
        }

        return PartyStateResponse.builder()
                .partyId(party.getPartyId())
                .leaderId(party.getLeaderId())
                .videoUrl(party.getVideoUrl())
                .currentTimeStamp(party.getCurrentTimeStamp())
                .isPlaying(party.isPlaying())
                .members(party.getJoinedMembers())
                .activeMembers(party.getActiveMembers())
                .pendingInviteUserIds(resolvePendingInviteUserIds(party, userId))
                .build();
    }

    private Set<String> resolvePendingInviteUserIds(WatchParty party, Long requesterUserId) {
        if (!party.getLeaderId().equals(String.valueOf(requesterUserId))) {
            return Set.of();
        }

        return party.getPendingInvites().values().stream()
                .filter(invite -> !invite.isExpired())
                .map(PendingInvite::friendId)
                .collect(Collectors.toSet());
    }
}
