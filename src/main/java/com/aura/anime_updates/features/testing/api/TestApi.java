package com.aura.anime_updates.features.testing.api;

import com.aura.anime_updates.features.fireBaseToken.domain.entity.FcmToken;
import com.aura.anime_updates.features.fireBaseToken.domain.repository.FcmTokenRepository;
import com.aura.anime_updates.features.fireBaseToken.domain.service.FcmNotificationService;
import com.aura.anime_updates.features.release.api.response.ReleaseInfoResponse;
import com.aura.anime_updates.features.release.domain.mapper.ReleaseMapper;
import com.aura.anime_updates.features.release.domain.repository.ReleaseRepository;
import com.aura.anime_updates.features.user.domain.repository.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestApi {

    private final String PASSWORD_ENCRYPTED = "$2a$12$LCAtczpKqtkL66QJkjvqGeSdMxhirWu.muV64qUa/H7J3PUMKAp0y";
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final FcmNotificationService notiService;
    private final UserRepository userRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final ReleaseRepository releaseRepository;
    private final ReleaseMapper releaseMapper;

    @GetMapping("/noti")
    public String sendNoti(@RequestParam String password) {
        if(!passwordEncoder.matches(password, PASSWORD_ENCRYPTED)) {
            return null;
        }

        ReleaseInfoResponse releaseInfoResponse = releaseMapper.toResponse(releaseRepository.findReleaseById(1L).get());

        Notification notification = Notification.builder()
                .setTitle("Title")
                .setBody("Body")
                .setImage("https://www.dictionary.com/e/wp-content/uploads/2018/03/rickrolling.jpg")
                .build();

        FcmToken token = fcmTokenRepository.findById(1L).get();
        Message message = Message.builder()
                .setToken(token.getToken())
                .setNotification(notification)
                .putAllData(releaseMapper.toMap(releaseInfoResponse))
                .build();

        try {

            FirebaseMessaging.getInstance().send(message);

        } catch(Exception e) {
            System.out.println("Error: " + e);
        }

        return "Test Complete";
    }
}