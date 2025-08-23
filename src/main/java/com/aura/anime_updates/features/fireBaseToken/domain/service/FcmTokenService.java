package com.aura.anime_updates.features.fireBaseToken.domain.service;

import com.aura.anime_updates.features.fireBaseToken.domain.entity.FcmToken;
import com.aura.anime_updates.features.user.domain.entity.User;
import com.aura.anime_updates.features.fireBaseToken.api.request.FcmTokenRequest;
import com.aura.anime_updates.features.fireBaseToken.domain.repository.FcmTokenRepository;
import com.aura.anime_updates.features.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    public void registerToken(Long userId, FcmTokenRequest request) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User Not Found"));

            String token = request.getToken();
            String deviceId = request.getDeviceId();

            if(token == null || token.isBlank() || deviceId == null || deviceId.isBlank()) {
                throw new IllegalArgumentException("Token with device id not provided");
            }

            fcmTokenRepository.findByDeviceId(deviceId)
                    .ifPresentOrElse(existing -> {
                        existing.setToken(token);
                        existing.setDeviceId(deviceId);
                        existing.setActive(true);
                        existing.setUser(user);
                        existing.setUpdatedAt(LocalDateTime.now());
                        fcmTokenRepository.save(existing);
                    }, () -> {
                        FcmToken newToken = FcmToken.builder()
                                .token(token)
                                .deviceId(deviceId)
                                .user(user)
                                .active(true)
                                .build();
                        fcmTokenRepository.save(newToken);
                    });
        } catch(Exception e) {
            System.out.println("Error registering token : " + e);
        }
    }

    public List<FcmToken> getActiveTokensForUser(Long userId) {
        return fcmTokenRepository.findAllByUserIdAndActiveTrue(userId);
    }
}
