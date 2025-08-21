package com.aura.anime_updates.services;

import com.aura.anime_updates.domain.FcmToken;
import com.aura.anime_updates.domain.User;
import com.aura.anime_updates.repository.FcmTokenRepository;
import com.aura.anime_updates.repository.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.hibernate.internal.util.collections.ArrayHelper.forEach;

@Service
@RequiredArgsConstructor
public class FcmNotificationService {

    private final FcmTokenRepository fcmTokenRepository;

    public void sendNotificationToAllDevicesOfUsers(List<User> users, Notification notification) {
        List<FcmToken> tokensToSendTo = fcmTokenRepository.findAllByUserInAndActiveTrue(users);

        if(!tokensToSendTo.isEmpty()) {
            for (FcmToken token : tokensToSendTo) {
                try {
                    Message message = Message.builder()
                            .setToken(token.getToken())
                            .setNotification(notification)
                            .build();

                    FirebaseMessaging.getInstance().send(message);
                } catch (Exception e) {
                    System.out.println("Error in Firebase Messaging: " + e.getMessage());
                }
            }
        }
    }
}
