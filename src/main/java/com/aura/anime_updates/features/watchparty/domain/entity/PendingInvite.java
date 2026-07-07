package com.aura.anime_updates.features.watchparty.domain.entity;

public record PendingInvite(String friendId, long expiresAt) {

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}
