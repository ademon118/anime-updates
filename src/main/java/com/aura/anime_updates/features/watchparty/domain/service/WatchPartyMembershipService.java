package com.aura.anime_updates.features.watchparty.domain.service;

import com.aura.anime_updates.features.watchparty.api.SyncAction;
import com.aura.anime_updates.features.watchparty.domain.entity.WatchParty;
import com.aura.anime_updates.features.watchparty.enums.SyncActionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Manages who is in a party ({@code joinedMembers}) vs who is currently connected ({@code activeMembers}).
 * <ul>
 *   <li>WebSocket disconnect → offline immediately, grace timer starts</li>
 *   <li>Grace expires → removed from party (same as leave)</li>
 *   <li>Explicit LEAVE → removed from party immediately</li>
 * </ul>
 */
@Slf4j
@Service
public class WatchPartyMembershipService {

    private final WatchPartyManager partyManager;
    private final CleanupService presenceScheduler;
    private final SimpMessagingTemplate messagingTemplate;

    public WatchPartyMembershipService(
            WatchPartyManager partyManager,
            CleanupService presenceScheduler,
            @Lazy SimpMessagingTemplate messagingTemplate
    ) {
        this.partyManager = partyManager;
        this.presenceScheduler = presenceScheduler;
        this.messagingTemplate = messagingTemplate;
    }

    public void markOnline(String partyId, String userId) {
        presenceScheduler.cancelOfflineGracePeriod(partyId, userId);
        partyManager.getParty(partyId).ifPresent(party -> {
            party.getActiveMembers().add(userId);
            log.info("Watch party member online partyId={} userId={}", partyId, userId);
        });
    }

    public void markOffline(String partyId, String userId) {
        partyManager.getParty(partyId).ifPresent(party -> {
            party.getActiveMembers().remove(userId);
            log.info("Watch party member offline partyId={} userId={}", partyId, userId);
        });

        presenceScheduler.scheduleOfflineGracePeriod(
                partyId,
                userId,
                () -> removeJoinedMemberAfterGraceExpiry(partyId, userId)
        );
    }

    /**
     * @return LEAVE broadcast for {@code @SendTo}; leader-change (if any) is published separately
     */
    public Optional<SyncAction> leaveExplicitly(String partyId, String userId, String senderName) {
        return removeJoinedMember(partyId, userId, senderName, LeaveCause.EXPLICIT)
                .map(result -> {
                    result.leaderChangeBroadcast()
                            .ifPresent(action -> publishToPartyTopic(result.partyId(), action));
                    return result.leaveBroadcast();
                });
    }

    private void removeJoinedMemberAfterGraceExpiry(String partyId, String userId) {
        removeJoinedMember(partyId, userId, userId, LeaveCause.GRACE_EXPIRED)
                .ifPresent(this::publishLeaveResult);
    }

    private Optional<MemberLeaveResult> removeJoinedMember(
            String partyId,
            String userId,
            String senderName,
            LeaveCause cause
    ) {
        WatchParty party = partyManager.getParty(partyId).orElse(null);
        if (party == null) {
            log.info("Skip member removal, party gone partyId={} userId={} cause={}", partyId, userId, cause);
            return Optional.empty();
        }

        if (!party.getJoinedMembers().contains(userId)) {
            presenceScheduler.cancelOfflineGracePeriod(partyId, userId);
            return Optional.empty();
        }

        presenceScheduler.cancelOfflineGracePeriod(partyId, userId);

        boolean departingUserWasLeader = userId.equals(party.getLeaderId());
        party.removeMember(userId);

        SyncAction leaveBroadcast = SyncAction.builder()
                .action(SyncActionType.LEAVE)
                .senderUsername(senderName)
                .leaderId(party.getLeaderId())
                .build();

        log.info(
                "Watch party member removed partyId={} userId={} cause={} remainingMembers={}",
                partyId,
                userId,
                cause,
                party.getJoinedMembers()
        );

        if (party.getJoinedMembers().isEmpty()) {
            partyManager.removeParty(partyId);
            log.info("Watch party dissolved (empty) partyId={}", partyId);
            return Optional.of(MemberLeaveResult.dissolved(partyId, leaveBroadcast));
        }

        Optional<SyncAction> leaderChangeBroadcast = Optional.empty();
        if (departingUserWasLeader) {
            leaderChangeBroadcast = resolveDepartureOfLeader(partyId, party);
            if (partyManager.getParty(partyId).isEmpty()) {
                return Optional.of(MemberLeaveResult.dissolved(partyId, leaveBroadcast));
            }
        } else {
            partyManager.cleanupIfAbandoned(partyId, party);
        }

        return Optional.of(MemberLeaveResult.remaining(partyId, leaveBroadcast, leaderChangeBroadcast));
    }

    /**
     * @return leader-change broadcast when leadership moves; empty when the party was dissolved
     */
    private Optional<SyncAction> resolveDepartureOfLeader(String partyId, WatchParty party) {
        if (party.getJoinedMembers().isEmpty()) {
            partyManager.removeParty(partyId);
            log.info("Watch party dissolved (leader left alone) partyId={}", partyId);
            return Optional.empty();
        }

        Optional<String> newLeaderId = partyManager.transferLeadership(party);
        if (newLeaderId.isEmpty()) {
            partyManager.removeParty(partyId);
            log.info("Watch party dissolved (leadership transfer failed) partyId={}", partyId);
            return Optional.empty();
        }

        log.info("Watch party leadership transferred partyId={} newLeaderId={}", partyId, newLeaderId.get());
        return Optional.of(SyncAction.builder()
                .action(SyncActionType.LEADER_CHANGE)
                .leaderId(newLeaderId.get())
                .build());
    }

    private void publishLeaveResult(MemberLeaveResult result) {
        publishToPartyTopic(result.partyId(), result.leaveBroadcast());
        result.leaderChangeBroadcast()
                .ifPresent(action -> publishToPartyTopic(result.partyId(), action));
    }

    private void publishToPartyTopic(String partyId, SyncAction action) {
        messagingTemplate.convertAndSend("/topic/party/" + partyId, action);
        log.info("Watch party published {} partyId={}", action.action(), partyId);
    }

    private enum LeaveCause {
        EXPLICIT,
        GRACE_EXPIRED
    }
}
