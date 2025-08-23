package com.aura.anime_updates.features.authentication.api.request;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LogoutRequest {

    String refreshToken;
    String fcmToken;
}
