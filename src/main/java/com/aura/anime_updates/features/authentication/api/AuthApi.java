package com.aura.anime_updates.features.authentication.api;

import com.aura.anime_updates.dto.ApiResponse;
import com.aura.anime_updates.features.authentication.api.request.AuthRequest;
import com.aura.anime_updates.features.authentication.api.response.AuthResponse;
import com.aura.anime_updates.features.authentication.domain.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthApi {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@RequestBody AuthRequest request){
        try {
            AuthResponse authResponse = authService.register(request);
            if (authResponse.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.successWithToken(authResponse.getToken(), authResponse.getMessage()));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error(authResponse.getMessage(), 400));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Registration failed: " + e.getMessage(), 400));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody AuthRequest request){
        try {
            AuthResponse authResponse = authService.login(request);
            if (authResponse.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.successWithToken(authResponse.getToken(), authResponse.getMessage()));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error(authResponse.getMessage(), 400));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Login failed: " + e.getMessage(), 400));
        }
    }
}