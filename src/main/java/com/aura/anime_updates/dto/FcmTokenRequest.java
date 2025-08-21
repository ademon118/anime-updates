package com.aura.anime_updates.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class FcmTokenRequest {
    private String token;
    private String deviceId;
}
