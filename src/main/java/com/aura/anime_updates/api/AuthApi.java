package com.aura.anime_updates.api;

import com.aura.anime_updates.domain.User;
import com.aura.anime_updates.dto.ApiResponse;
import com.aura.anime_updates.dto.AuthRequest;
import com.aura.anime_updates.dto.AuthResponse;
import com.aura.anime_updates.repository.UserRepository;
import com.aura.anime_updates.security.JwtUtil;
import com.aura.anime_updates.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    public ResponseEntity<ApiResponse<String>> register(@RequestBody AuthRequest request){
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
    public ResponseEntity<ApiResponse<String>> login(@RequestBody AuthRequest request){
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