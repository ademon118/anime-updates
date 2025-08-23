package com.aura.anime_updates.features.fireBaseToken.api.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class FcmTokenRequest {
    private String token;
    private String deviceId;
}
