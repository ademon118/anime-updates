package com.aura.anime_updates.features.watchparty.domain.service;

import com.aura.anime_updates.features.watchparty.domain.entity.WatchParty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class WatchPartyManager {
    private final Map<String, WatchParty> activeParties = new ConcurrentHashMap<>();
    private final Map<String, String> activePartyIdByLeaderUsername = new ConcurrentHashMap<>();
    private final Map<String, Object> leaderInviteLocks = new ConcurrentHashMap<>();

    public Object leaderInviteLock(String leaderUsername) {
        return leaderInviteLocks.computeIfAbsent(leaderUsername, ignored -> new Object());
    }

    public WatchParty createParty(String leaderUsername, String partyId) {
        WatchParty party = WatchParty.builder()
                .partyId(partyId)
                .leaderUsername(leaderUsername)
                .isPlaying(false)
                .currentTimeStamp(0.0)
                .lastUpdated(System.currentTimeMillis())
                .build();
        party.addMember(leaderUsername);
        activeParties.put(partyId, party);
        registerActivePartyForLeader(leaderUsername, partyId);
        return party;
    }

    public Optional<WatchParty> getParty(String partyId) {
        return Optional.ofNullable(activeParties.get(partyId));
    }

    public Optional<WatchParty> findActivePartyForLeader(String leaderUsername) {
        String partyId = activePartyIdByLeaderUsername.get(leaderUsername);
        if (partyId == null) {
            return Optional.empty();
        }

        WatchParty party = activeParties.get(partyId);
        if (party == null) {
            activePartyIdByLeaderUsername.remove(leaderUsername, partyId);
            return Optional.empty();
        }

        if (!leaderUsername.equals(party.getLeaderUsername())) {
            activePartyIdByLeaderUsername.remove(leaderUsername, partyId);
            return Optional.empty();
        }

        return Optional.of(party);
    }

    public void registerActivePartyForLeader(String leaderUsername, String partyId) {
        activePartyIdByLeaderUsername.put(leaderUsername, partyId);
    }

    public void unregisterActivePartyForLeader(String leaderUsername, String partyId) {
        activePartyIdByLeaderUsername.remove(leaderUsername, partyId);
    }

    public void recordLeadershipTransfer(String partyId, String previousLeaderUsername, String newLeaderUsername) {
        unregisterActivePartyForLeader(previousLeaderUsername, partyId);
        registerActivePartyForLeader(newLeaderUsername, partyId);
    }

    public void removeParty(String partyId) {
        WatchParty party = activeParties.remove(partyId);
        if (party != null) {
            unregisterActivePartyForLeader(party.getLeaderUsername(), partyId);
        }
    }

    public boolean isMember(String partyId, String username) {
        return getParty(partyId)
                .map(p -> p.getJoinedMembers().contains(username))
                .orElse(false);
    }

    public boolean isLeader(String partyId, String username) {
        return getParty(partyId)
                .map(p -> p.getLeaderUsername().equals(username))
                .orElse(false);
    }

    public Optional<String> transferLeadership(WatchParty party) {
        if (party.getJoinedMembers().isEmpty()) {
            return Optional.empty();
        }

        List<String> candidates = new ArrayList<>(party.getJoinedMembers());
        String newLeaderUsername = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        String previousLeaderUsername = party.getLeaderUsername();
        party.setLeaderUsername(newLeaderUsername);
        recordLeadershipTransfer(party.getPartyId(), previousLeaderUsername, newLeaderUsername);
        return Optional.of(newLeaderUsername);
    }

    /**
     * Solo-leader parties are kept alive so the leader can invite again without restarting.
     * Parties are removed only when the last member leaves through the normal leave flow.
     */
    public void cleanupIfAbandoned(String partyId, WatchParty party) {
        // Intentionally no-op: do not dissolve while the leader remains in the party.
    }
}
