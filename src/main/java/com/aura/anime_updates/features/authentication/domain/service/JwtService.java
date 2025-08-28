package com.aura.anime_updates.features.authentication.domain.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.issuer}")
    private String issuer;

    @Value("${security.jwt.access-token-ttl-minutes}")
    private long accessMinutes;

    @Value("${security.jwt.refresh-token-ttl-days}")
    private long refreshDays;

    private Key key;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String createAccessToken(Long userId, String username, String refreshJti) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessMinutes * 60);
        return Jwts.builder()
                .header().type("JWT").and()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("typ", "access")
                .claim("rt", refreshJti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .id(UUID.randomUUID().toString())
                .signWith(getSigningKey())
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(refreshDays * 86400);
        return Jwts.builder()
                .header().type("JWT").and()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .claim("typ", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .id(UUID.randomUUID().toString()) // jti for rotation tracking
                .signWith(getSigningKey())
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
    }

    public boolean isAccess(Jws<Claims> jws) {
        return "access".equals(jws.getPayload().get("typ"));
    }

    public boolean isRefresh(Jws<Claims> jws) {
        return "refresh".equals(jws.getPayload().get("typ"));
    }

    public Long getUserId(Jws<Claims> jws) {
        return Long.valueOf(jws.getPayload().getSubject());
    }

    public String getJti(Jws<Claims> jws) {
        return jws.getPayload().getId();
    }

    public String getRefreshJtiFromAccess(Jws<Claims> jws) {
        return jws.getPayload().get("rt", String.class);
    }
}
