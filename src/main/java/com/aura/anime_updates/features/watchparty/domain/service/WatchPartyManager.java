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
    private final Map<String, String> activePartyIdByLeaderId = new ConcurrentHashMap<>();
    private final Map<String, Object> leaderInviteLocks = new ConcurrentHashMap<>();

    public Object leaderInviteLock(String leaderId) {
        return leaderInviteLocks.computeIfAbsent(leaderId, ignored -> new Object());
    }

    public WatchParty createParty(String leaderId, String partyId) {
        WatchParty party = WatchParty.builder()
                .partyId(partyId)
                .leaderId(leaderId)
                .isPlaying(false)
                .currentTimeStamp(0.0)
                .lastUpdated(System.currentTimeMillis())
                .build();
        party.addMember(leaderId);
        activeParties.put(partyId, party);
        registerActivePartyForLeader(leaderId, partyId);
        return party;
    }

    public Optional<WatchParty> getParty(String partyId) {
        return Optional.ofNullable(activeParties.get(partyId));
    }

    public Optional<WatchParty> findActivePartyForLeader(String leaderId) {
        String partyId = activePartyIdByLeaderId.get(leaderId);
        if (partyId == null) {
            return Optional.empty();
        }

        WatchParty party = activeParties.get(partyId);
        if (party == null) {
            activePartyIdByLeaderId.remove(leaderId, partyId);
            return Optional.empty();
        }

        if (!leaderId.equals(party.getLeaderId())) {
            activePartyIdByLeaderId.remove(leaderId, partyId);
            return Optional.empty();
        }

        return Optional.of(party);
    }

    public void registerActivePartyForLeader(String leaderId, String partyId) {
        activePartyIdByLeaderId.put(leaderId, partyId);
    }

    public void unregisterActivePartyForLeader(String leaderId, String partyId) {
        activePartyIdByLeaderId.remove(leaderId, partyId);
    }

    public void recordLeadershipTransfer(String partyId, String previousLeaderId, String newLeaderId) {
        unregisterActivePartyForLeader(previousLeaderId, partyId);
        registerActivePartyForLeader(newLeaderId, partyId);
    }

    public void removeParty(String partyId) {
        WatchParty party = activeParties.remove(partyId);
        if (party != null) {
            unregisterActivePartyForLeader(party.getLeaderId(), partyId);
        }
    }

    public boolean isMember(String partyId, String userId) {
        return getParty(partyId)
                .map(p -> p.getJoinedMembers().contains(userId))
                .orElse(false);
    }

    public boolean isLeader(String partyId, String userId) {
        return getParty(partyId)
                .map(p -> p.getLeaderId().equals(userId))
                .orElse(false);
    }

    public Optional<String> transferLeadership(WatchParty party) {
        if (party.getJoinedMembers().isEmpty()) {
            return Optional.empty();
        }

        List<String> candidates = new ArrayList<>(party.getJoinedMembers());
        String newLeader = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        String previousLeaderId = party.getLeaderId();
        party.setLeaderId(newLeader);
        recordLeadershipTransfer(party.getPartyId(), previousLeaderId, newLeader);
        return Optional.of(newLeader);
    }

    public void cleanupIfAbandoned(String partyId, WatchParty party) {
        boolean leaderAlone = party.getJoinedMembers().size() == 1
                && party.getJoinedMembers().contains(party.getLeaderId());
        if (leaderAlone && party.getPendingInvites().isEmpty()) {
            removeParty(partyId);
        }
    }
}
