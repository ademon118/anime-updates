package com.aura.anime_updates.features.watchparty.api;

import com.aura.anime_updates.features.watchparty.enums.SyncActionType;
import lombok.Builder;

import java.util.Set;

@Builder
public record SyncAction(
        SyncActionType action,
        double timestamp,
        boolean isPlaying,
        String videoUrl,
        String senderUsername,
        String leaderUsername,
        Set<String> activeMembers,
        Set<String> members
) {
    public SyncAction withSender(String senderUsername) {
        return new SyncAction(
                action,
                timestamp,
                isPlaying,
                videoUrl,
                senderUsername,
                leaderUsername,
                activeMembers,
                members
        );
    }
}
