package com.aura.anime_updates.features.watchparty.domain.entity;

public record PendingInvite(String inviteeUsername, long expiresAt) {

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}
