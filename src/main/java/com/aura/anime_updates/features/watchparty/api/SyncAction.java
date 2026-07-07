package com.aura.anime_updates.features.watchparty.api;

import com.aura.anime_updates.features.watchparty.enums.SyncActionType;
import lombok.Builder;

@Builder
public record SyncAction(
        SyncActionType action,
        double timestamp,
        boolean isPlaying,
        String videoUrl,
        String senderUsername,
        String leaderId
) {
    public SyncAction withSender(String senderUsername) {
        return new SyncAction(action, timestamp, isPlaying, videoUrl, senderUsername, leaderId);
    }
}
