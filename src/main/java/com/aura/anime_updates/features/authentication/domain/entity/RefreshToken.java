package com.aura.anime_updates.features.authentication.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "ix_refresh_user", columnList = "userId"),
        @Index(name = "ix_refresh_jti", columnList = "jti", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false, unique = true)
    private String jti;

    @Column(nullable = false)
    private String tokenHash;

    private Instant expiresAt;
    private Instant createdAt;
    private Instant revokedAt;

    private String replacedByJti;

    public boolean isActive() {
        return revokedAt == null && Instant.now().isBefore(expiresAt);
    }
}
