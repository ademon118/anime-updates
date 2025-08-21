package com.aura.anime_updates.repository;

import com.aura.anime_updates.domain.FcmToken;
import com.aura.anime_updates.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByToken(String token);

    Optional<FcmToken> findByDeviceId(String deviceId);

    List<FcmToken> findAllByUserIdAndActiveTrue(Long userId);

    List<FcmToken> findAllByActiveTrue();

    void deleteByToken(String token);
}