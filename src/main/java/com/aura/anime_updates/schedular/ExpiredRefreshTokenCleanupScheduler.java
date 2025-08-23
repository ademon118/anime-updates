package com.aura.anime_updates.schedular;

import com.aura.anime_updates.features.authentication.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiredRefreshTokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Cleanup expired refresh tokens daily at midnight (UTC)
     *
     */
    @Scheduled(cron = "0 0 0 * * ?", zone = "UTC")
    public void cleanupExpiredRefreshTokens() {
        try {
            Instant now = Instant.now();
            int deleted = refreshTokenRepository.deleteByExpiresAtBefore(now);
            log.info("Cleaned up {} expired refresh tokens at {}", deleted, now);
        } catch (Exception e) {
            log.error("Error occurred while cleaning up expired refresh tokens", e);
        }
    }
}