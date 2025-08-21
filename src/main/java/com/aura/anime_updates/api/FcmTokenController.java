package com.aura.anime_updates.api;

import com.aura.anime_updates.dto.ApiResponse;
import com.aura.anime_updates.dto.FcmTokenRequest;
import com.aura.anime_updates.dto.FcmTokenResponse;
import com.aura.anime_updates.security.CustomUserDetails;
import com.aura.anime_updates.services.FcmTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fcm")
public class FcmTokenController {

    @Autowired
    private FcmTokenService fcmTokenService;

    @PostMapping("/register")
    public ApiResponse<Object> registerToken(@RequestBody FcmTokenRequest request, Authentication auth) {
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Long userId = userDetails.getId();
        fcmTokenService.registerToken(userId, request);
        return ApiResponse.builder()
                .success(true)
                .message("FCM Token Registration Successful")
                .build();
    }
}
