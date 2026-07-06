package com.aura.anime_updates.features.friends.api.request;

import lombok.Builder;

@Builder
public record FriendRequestDTO(
        String username
) { }
