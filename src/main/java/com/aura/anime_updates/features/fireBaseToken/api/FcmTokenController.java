package com.aura.anime_updates.features.fireBaseToken.api;

import com.aura.anime_updates.dto.ApiResponse;
import com.aura.anime_updates.features.fireBaseToken.api.request.FcmTokenRequest;
import com.aura.anime_updates.security.CustomUserDetails;
import com.aura.anime_updates.features.fireBaseToken.domain.service.FcmTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fcm")
@Tag(name = "FCM Token", description = "APIs for managing Firebase Cloud Messaging tokens")
public class FcmTokenController {

    @Autowired
    private FcmTokenService fcmTokenService;

    @Operation(summary = "Register FCM token", description = "Register Firebase Cloud Messaging token for push notifications")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "FCM token registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - valid access token required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/register")
    @PreAuthorize("isAuthenticated()")
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
