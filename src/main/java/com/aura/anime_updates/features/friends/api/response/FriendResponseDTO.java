package com.aura.anime_updates.features.friends.api.response;

import lombok.Builder;

@Builder
public record FriendResponseDTO(
        Long id,
        String username,
        String status,
        boolean isSender
) { }
