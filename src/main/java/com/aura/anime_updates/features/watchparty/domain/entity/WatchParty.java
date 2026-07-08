package com.aura.anime_updates.features.watchparty.domain.entity;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Builder
public class WatchParty {
    private final String partyId;
    private String leaderUsername;

    private final Map<String, PendingInvite> pendingInvites = new ConcurrentHashMap<>();

    /** Joined party members, keyed by unique username. */
    private final Set<String> joinedMembers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    /** Currently connected members, keyed by unique username. */
    private final Set<String> activeMembers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private String videoUrl;
    private double currentTimeStamp;
    private boolean isPlaying;
    private long lastUpdated;

    public void addMember(String username) {
        joinedMembers.add(username);
    }

    public void removeMember(String username) {
        joinedMembers.remove(username);
        activeMembers.remove(username);
    }
}
