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
        return party;
    }

    public Optional<WatchParty> getParty(String partyId) {
        return Optional.ofNullable(activeParties.get(partyId));
    }

    public void removeParty(String partyId) {
        activeParties.remove(partyId);
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
        party.setLeaderId(newLeader);
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
