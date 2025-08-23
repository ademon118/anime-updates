package com.aura.anime_updates.features.authentication.api.response;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Value
@Builder
public class AuthResponse {
    String token;
    boolean success;
    String message;
}
