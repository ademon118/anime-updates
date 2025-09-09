package com.aura.anime_updates.features.fireBaseToken.domain.service;

import com.aura.anime_updates.features.fireBaseToken.domain.entity.FcmToken;
import com.aura.anime_updates.features.user.domain.entity.User;
import com.aura.anime_updates.features.fireBaseToken.domain.repository.FcmTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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
                } catch (FirebaseMessagingException e) {
                    token.deactivate();
                    fcmTokenRepository.save(token);
                    log.warn("Deactivated invalid token: " + token.getToken());
                }catch (Exception e){
                    log.error("Error in Firebase Messaging: " + e.getMessage());
                }
            }
        }
    }
}
