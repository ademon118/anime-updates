package com.aura.anime_updates.features.authentication.api.response;

import lombok.Builder;

@Builder
public record AuthResponse (
    String accessToken,
    String refreshToken
) { }
