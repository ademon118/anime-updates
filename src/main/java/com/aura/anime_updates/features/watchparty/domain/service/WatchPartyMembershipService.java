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
 * Both sets store unique usernames.
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

    public void markOnline(String partyId, String username) {
        presenceScheduler.cancelOfflineGracePeriod(partyId, username);
        partyManager.getParty(partyId).ifPresent(party -> {
            party.getActiveMembers().add(username);
            log.info("Watch party member online partyId={} username={}", partyId, username);
            publishPresence(partyId, party);
        });
    }

    public void markOffline(String partyId, String username) {
        partyManager.getParty(partyId).ifPresent(party -> {
            party.getActiveMembers().remove(username);
            log.info("Watch party member offline partyId={} username={}", partyId, username);
            publishPresence(partyId, party);
        });

        presenceScheduler.scheduleOfflineGracePeriod(
                partyId,
                username,
                () -> removeJoinedMemberAfterGraceExpiry(partyId, username)
        );
    }

    /**
     * @return LEAVE broadcast for {@code @SendTo}; leader-change (if any) is published separately
     */
    public Optional<SyncAction> leaveExplicitly(String partyId, String username) {
        return removeJoinedMember(partyId, username, LeaveCause.EXPLICIT)
                .map(result -> {
                    result.leaderChangeBroadcast()
                            .ifPresent(action -> publishToPartyTopic(result.partyId(), action));
                    return result.leaveBroadcast();
                });
    }

    private void removeJoinedMemberAfterGraceExpiry(String partyId, String username) {
        removeJoinedMember(partyId, username, LeaveCause.GRACE_EXPIRED)
                .ifPresent(this::publishLeaveResult);
    }

    private Optional<MemberLeaveResult> removeJoinedMember(
            String partyId,
            String username,
            LeaveCause cause
    ) {
        WatchParty party = partyManager.getParty(partyId).orElse(null);
        if (party == null) {
            log.info("Skip member removal, party gone partyId={} username={} cause={}", partyId, username, cause);
            return Optional.empty();
        }

        if (!party.getJoinedMembers().contains(username)) {
            presenceScheduler.cancelOfflineGracePeriod(partyId, username);
            return Optional.empty();
        }

        presenceScheduler.cancelOfflineGracePeriod(partyId, username);

        boolean departingMemberWasLeader = username.equals(party.getLeaderUsername());
        party.removeMember(username);

        log.info(
                "Watch party member removed partyId={} username={} cause={} remainingMembers={}",
                partyId,
                username,
                cause,
                party.getJoinedMembers()
        );

        if (party.getJoinedMembers().isEmpty()) {
            partyManager.removeParty(partyId);
            log.info("Watch party dissolved (empty) partyId={}", partyId);
            return Optional.of(MemberLeaveResult.dissolved(
                    partyId,
                    syncPublisher.buildLeaveBroadcast(username, party.getLeaderUsername(), Set.of(), Set.of())
            ));
        }

        Optional<SyncAction> leaderChangeBroadcast = Optional.empty();
        if (departingMemberWasLeader) {
            leaderChangeBroadcast = resolveDepartureOfLeader(partyId, party);
            if (partyManager.getParty(partyId).isEmpty()) {
                return Optional.of(MemberLeaveResult.dissolved(
                        partyId,
                        syncPublisher.buildLeaveBroadcast(username, party.getLeaderUsername(), Set.of(), Set.of())
                ));
            }
        } else {
            partyManager.cleanupIfAbandoned(partyId, party);
        }

        return Optional.of(MemberLeaveResult.remaining(
                partyId,
                syncPublisher.buildLeaveBroadcast(
                        username,
                        party.getLeaderUsername(),
                        Set.copyOf(party.getJoinedMembers()),
                        Set.copyOf(party.getActiveMembers())
                ),
                leaderChangeBroadcast
        ));
    }

    private Optional<SyncAction> resolveDepartureOfLeader(String partyId, WatchParty party) {
        if (party.getJoinedMembers().isEmpty()) {
            partyManager.removeParty(partyId);
            log.info("Watch party dissolved (leader left alone) partyId={}", partyId);
            return Optional.empty();
        }

        Optional<String> newLeaderUsername = partyManager.transferLeadership(party);
        if (newLeaderUsername.isEmpty()) {
            partyManager.removeParty(partyId);
            log.info("Watch party dissolved (leadership transfer failed) partyId={}", partyId);
            return Optional.empty();
        }

        String promotedLeaderUsername = newLeaderUsername.get();
        resetGraceForPromotedLeader(partyId, party, promotedLeaderUsername);
        log.info("Watch party leadership transferred partyId={} newLeaderUsername={}", partyId, promotedLeaderUsername);
        return Optional.of(SyncAction.builder()
                .action(SyncActionType.LEADER_CHANGE)
                .leaderUsername(promotedLeaderUsername)
                .build());
    }

    private void resetGraceForPromotedLeader(String partyId, WatchParty party, String newLeaderUsername) {
        presenceScheduler.cancelOfflineGracePeriod(partyId, newLeaderUsername);
        if (!party.getActiveMembers().contains(newLeaderUsername)) {
            presenceScheduler.scheduleOfflineGracePeriod(
                    partyId,
                    newLeaderUsername,
                    () -> removeJoinedMemberAfterGraceExpiry(partyId, newLeaderUsername)
            );
            log.info(
                    "Watch party fresh offline grace for promoted leader partyId={} username={}",
                    partyId,
                    newLeaderUsername
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
