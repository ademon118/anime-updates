package com.aura.anime_updates.features.watchparty.domain.service;

import com.aura.anime_updates.features.watchparty.api.SyncAction;
import com.aura.anime_updates.features.watchparty.domain.entity.WatchParty;
import com.aura.anime_updates.features.watchparty.enums.SyncActionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

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
    private final WatchPartySyncPublisher syncPublisher;

    public WatchPartyMembershipService(
            WatchPartyManager partyManager,
            CleanupService presenceScheduler,
            @Lazy WatchPartySyncPublisher syncPublisher
    ) {
        this.partyManager = partyManager;
        this.presenceScheduler = presenceScheduler;
        this.syncPublisher = syncPublisher;
    }

    public void markOnline(String partyId, String userId) {
        presenceScheduler.cancelOfflineGracePeriod(partyId, userId);
        partyManager.getParty(partyId).ifPresent(party -> {
            party.getActiveMembers().add(userId);
            log.info("Watch party member online partyId={} userId={}", partyId, userId);
            publishPresence(partyId, party);
        });
    }

    public void markOffline(String partyId, String userId) {
        partyManager.getParty(partyId).ifPresent(party -> {
            party.getActiveMembers().remove(userId);
            log.info("Watch party member offline partyId={} userId={}", partyId, userId);
            publishPresence(partyId, party);
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
            return Optional.of(MemberLeaveResult.dissolved(
                    partyId,
                    syncPublisher.buildLeaveBroadcast(senderName, party.getLeaderId(), Set.of())
            ));
        }

        Optional<SyncAction> leaderChangeBroadcast = Optional.empty();
        if (departingUserWasLeader) {
            leaderChangeBroadcast = resolveDepartureOfLeader(partyId, party);
            if (partyManager.getParty(partyId).isEmpty()) {
                return Optional.of(MemberLeaveResult.dissolved(
                        partyId,
                        syncPublisher.buildLeaveBroadcast(senderName, party.getLeaderId(), Set.of())
                ));
            }
        } else {
            partyManager.cleanupIfAbandoned(partyId, party);
        }

        return Optional.of(MemberLeaveResult.remaining(
                partyId,
                syncPublisher.buildLeaveBroadcast(
                        senderName,
                        party.getLeaderId(),
                        Set.copyOf(party.getJoinedMembers())
                ),
                leaderChangeBroadcast
        ));
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

        String promotedLeaderId = newLeaderId.get();
        resetGraceForPromotedLeader(partyId, party, promotedLeaderId);
        log.info("Watch party leadership transferred partyId={} newLeaderId={}", partyId, promotedLeaderId);
        return Optional.of(SyncAction.builder()
                .action(SyncActionType.LEADER_CHANGE)
                .leaderId(promotedLeaderId)
                .build());
    }

    /**
     * Cancels any stale offline grace for the promoted leader and, when they are still disconnected,
     * starts a fresh grace window from the transfer moment so they are not removed seconds later.
     */
    private void resetGraceForPromotedLeader(String partyId, WatchParty party, String newLeaderId) {
        presenceScheduler.cancelOfflineGracePeriod(partyId, newLeaderId);
        if (!party.getActiveMembers().contains(newLeaderId)) {
            presenceScheduler.scheduleOfflineGracePeriod(
                    partyId,
                    newLeaderId,
                    () -> removeJoinedMemberAfterGraceExpiry(partyId, newLeaderId)
            );
            log.info(
                    "Watch party fresh offline grace for promoted leader partyId={} userId={}",
                    partyId,
                    newLeaderId
            );
        }
    }

    private void publishLeaveResult(MemberLeaveResult result) {
        syncPublisher.publishToParty(result.partyId(), result.leaveBroadcast());
        result.leaderChangeBroadcast()
                .ifPresent(action -> syncPublisher.publishToParty(result.partyId(), action));
    }

    private void publishPresence(String partyId, WatchParty party) {
        syncPublisher.publishPresence(partyId, Set.copyOf(party.getActiveMembers()));
    }

    private void publishToPartyTopic(String partyId, SyncAction action) {
        syncPublisher.publishToParty(partyId, action);
    }

    private enum LeaveCause {
        EXPLICIT,
        GRACE_EXPIRED
    }
}
