package com.aura.anime_updates.schedular;

import com.aura.anime_updates.features.fireBaseToken.domain.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class FirebaseTokenDeactivateScheduler {

    private final FcmTokenRepository tokenRepository;

    @Value("${scheduler.deactivate-tokens.inactive-days}")
    private int inactiveDays;

    @Scheduled(cron = "${scheduler.deactivate-tokens.cron}", zone = "${scheduler.deactivate-tokens.zone}")
    public void deactivateFCMTokens() {

        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(inactiveDays);
            int updated = tokenRepository.deactivateInactiveTokens(cutoffDate);
            log.info("Deactivated {} account at {}", updated, LocalDateTime.now());
        } catch (Exception e) {
            log.error("Error occurred while deactivating old tokens", e);
        }

    }
}
