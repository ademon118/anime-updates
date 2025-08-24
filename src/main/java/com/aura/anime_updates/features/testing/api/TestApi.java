package com.aura.anime_updates.features.testing.api;

import com.aura.anime_updates.features.fireBaseToken.domain.entity.FcmToken;
import com.aura.anime_updates.features.fireBaseToken.domain.repository.FcmTokenRepository;
import com.aura.anime_updates.features.fireBaseToken.domain.service.FcmNotificationService;
import com.aura.anime_updates.features.release.api.response.ReleaseInfoResponse;
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

    private final String PASSWORD_ENCRYPTED = "$2a$12$w8G8Uq8tQF9ZyY1cM6QY9eD3rD7e5o5r3n9Xj3l7jzF1i3T8b3v7";
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final FcmNotificationService notiService;
    private final UserRepository userRepository;
    private final FcmTokenRepository fcmTokenRepository;

    @GetMapping("/noti")
    public String sendNoti(@RequestParam String password) {
        if(!passwordEncoder.matches(password, PASSWORD_ENCRYPTED)) {
            return null;
        }

        ReleaseInfoResponse releaseInfoResponse = new ReleaseInfoResponse(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );


        Notification notification = Notification.builder()
                .setTitle("Title")
                .setBody("Body")
                .setImage("https://www.dictionary.com/e/wp-content/uploads/2018/03/rickrolling.jpg")
                .build();

        FcmToken token = fcmTokenRepository.findById(1L).get();
        Message message = Message.builder()
                .setToken(token.getToken())
                .setNotification(notification)
                .putData("test key", "test value")
                .build();


        try {

            FirebaseMessaging.getInstance().send(message);

        } catch(Exception e) {
            System.out.println("Error: " + e);
        }
        return "Test Complete";
    }
}