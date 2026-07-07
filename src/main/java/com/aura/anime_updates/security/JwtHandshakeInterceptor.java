package com.aura.anime_updates.security;

import com.aura.anime_updates.features.authentication.domain.entity.RefreshToken;
import com.aura.anime_updates.features.authentication.domain.repository.RefreshTokenRepository;
import com.aura.anime_updates.features.authentication.domain.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Collections;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }

        String token = servletRequest.getServletRequest().getParameter("token");
        if (token == null || token.isBlank()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        try {
            Jws<Claims> jws = jwtService.parse(token);
            if (!jwtService.isAccess(jws)) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            String linkedRefreshJti = jwtService.getRefreshJtiFromAccess(jws);
            if (linkedRefreshJti == null) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            RefreshToken rt = refreshTokenRepository.findByJti(linkedRefreshJti).orElse(null);
            if (rt == null || !rt.isActive()) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            Long userId = jwtService.getUserId(jws);
            String username = (String) jws.getPayload().get("username");

            CustomUserDetails user = new CustomUserDetails(userId, username, "", Collections.emptyList());
            attributes.put("user", user);
            return true;
        } catch (JwtException e) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
