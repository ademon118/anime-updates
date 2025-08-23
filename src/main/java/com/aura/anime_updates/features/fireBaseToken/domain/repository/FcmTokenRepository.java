package com.aura.anime_updates.features.fireBaseToken.domain.repository;

import com.aura.anime_updates.features.fireBaseToken.domain.entity.FcmToken;
import com.aura.anime_updates.features.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByToken(String token);

    Optional<FcmToken> findByDeviceId(String deviceId);

    List<FcmToken> findAllByUserIdAndActiveTrue(Long userId);

    List<FcmToken> findAllByUserInAndActiveTrue(List<User> users);

    List<FcmToken> findAllByActiveTrue();

    void deleteByToken(String token);

    @Transactional
    @Modifying
    @Query("""
    UPDATE FcmToken f SET f.active = false WHERE f.updatedAt < :cutoffDate AND f.active = true
    """)
    int deactivateInactiveTokens(LocalDateTime cutoffDate);
}