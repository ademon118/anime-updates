package com.aura.anime_updates.features.authentication.domain.service;

import com.aura.anime_updates.features.authentication.api.request.AuthRequest;
import com.aura.anime_updates.features.authentication.api.request.LogoutRequest;
import com.aura.anime_updates.features.authentication.api.response.AuthResponse;
import com.aura.anime_updates.features.authentication.domain.entity.RefreshToken;
import com.aura.anime_updates.features.authentication.domain.repository.RefreshTokenRepository;
import com.aura.anime_updates.features.authentication.util.TokenHasher;
import com.aura.anime_updates.features.fireBaseToken.domain.service.FcmTokenService;
import com.aura.anime_updates.features.user.domain.service.UserService;
import com.aura.anime_updates.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final RefreshTokenRepository refreshRepo;
    private final TokenHasher hasher;
    private final UserService userService;
    private final FcmTokenService fcmTokenService;

    public AuthResponse register(AuthRequest request) {
        userService.createUser(request.getUserName(), request.getPassword());
        return authenticateAndGenerateTokens(request.getUserName(), request.getPassword());
    }

    public AuthResponse login(AuthRequest request) {
        return authenticateAndGenerateTokens(request.getUserName(), request.getPassword());
    }

    private AuthResponse authenticateAndGenerateTokens(String username, String password) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        Long userId = principal.getId();

        String access = jwt.createAccessToken(userId, principal.getUsername());
        String refresh = jwt.createRefreshToken(userId);

        Jws<Claims> jws = jwt.parse(refresh);
        String jti = jwt.getJti(jws);
        Instant exp = jws.getPayload().getExpiration().toInstant();

        refreshRepo.save(RefreshToken.builder()
                .userId(userId)
                .jti(jti)
                .tokenHash(hasher.hash(refresh))
                .createdAt(Instant.now())
                .expiresAt(exp)
                .build());

        return new AuthResponse(access, refresh);
    }

    public AuthResponse refresh(String rawRefreshToken) {
        Jws<Claims> jws = jwt.parse(rawRefreshToken);
        if (!jwt.isRefresh(jws)) throw new IllegalArgumentException("Not a refresh token");

        String jti = jwt.getJti(jws);
        Long userId = jwt.getUserId(jws);

        String username = jws.getPayload().get("username", String.class);

        RefreshToken db = refreshRepo.findByJti(jti)
                .orElseThrow(() -> new IllegalArgumentException("Unknown refresh token"));

        if (!db.isActive()) throw new IllegalStateException("Refresh token expired or revoked");

        if (!hasher.verify(rawRefreshToken, db.getTokenHash()))
            throw new IllegalStateException("Refresh token mismatch");

        String newAccess = jwt.createAccessToken(userId, username);
        String newRefresh = jwt.createRefreshToken(userId);

        Jws<Claims> newJws = jwt.parse(newRefresh);
        db.setRevokedAt(Instant.now());
        db.setReplacedByJti(jwt.getJti(newJws));
        refreshRepo.save(db);

        refreshRepo.save(RefreshToken.builder()
                .userId(userId)
                .jti(jwt.getJti(newJws))
                .tokenHash(hasher.hash(newRefresh))
                .createdAt(Instant.now())
                .expiresAt(newJws.getPayload().getExpiration().toInstant())
                .build());

        return new AuthResponse(newAccess, newRefresh);
    }

    public void logout(LogoutRequest logoutRequest) {

        Jws<Claims> jws = jwt.parse(logoutRequest.getRefreshToken());
        if (!jwt.isRefresh(jws)) return;
        refreshRepo.findByJti(jwt.getJti(jws)).ifPresent(rt -> {
            rt.setRevokedAt(Instant.now());
            refreshRepo.save(rt);
        });

        fcmTokenService.invalidateToken(logoutRequest.getFcmToken());
    }
}
