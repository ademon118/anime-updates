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
    private String leaderId;

    private final Map<String, PendingInvite> pendingInvites = new ConcurrentHashMap<>();

    private final Set<String> joinedMembers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<String> activeMembers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private String videoUrl;
    private double currentTimeStamp;
    private boolean isPlaying;
    private long lastUpdated;

    public void addMember(String userId) {
        joinedMembers.add(userId);
    }

    public void removeMember(String userId) {
        joinedMembers.remove(userId);
        activeMembers.remove(userId);
    }
}
