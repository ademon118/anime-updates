package com.aura.anime_updates.features.notification;

import com.aura.anime_updates.features.fireBaseToken.domain.entity.FcmToken;
import com.aura.anime_updates.features.fireBaseToken.domain.service.FcmTokenService;
import com.aura.anime_updates.features.user.domain.entity.User;
import com.aura.anime_updates.features.fireBaseToken.domain.repository.FcmTokenRepository;
import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final FcmTokenRepository fcmTokenRepository;
    private final FcmTokenService tokenService;

    public void sendNotificationToAllDevicesOfUsers(List<User> users, Notification notification, Map<String, String> dataPayload) {
        List<String> tokensToSendTo = fcmTokenRepository.findAllByUserInAndActiveTrue(users)
                .stream()
                .map(FcmToken::getToken)
                .toList();
        AndroidConfig androidConfig = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setTtl(86400L * 1000L)
                .setNotification(AndroidNotification.builder()
                        .setChannelId("anime_updates_channel")
                        .setPriority(AndroidNotification.Priority.HIGH)
                        .setVisibility(AndroidNotification.Visibility.PUBLIC)
                        .setSound("default")
                        .build())
                .build();

        if(!tokensToSendTo.isEmpty()) {
            try {
                MulticastMessage.Builder builder = MulticastMessage.builder()
                        .addAllTokens(tokensToSendTo)
                        .setNotification(notification)
                        .setAndroidConfig(androidConfig);

                if(dataPayload != null){
                    builder.putAllData(dataPayload);
                }
                
                MulticastMessage message = builder.build();

                ApiFuture<BatchResponse> future = FirebaseMessaging.getInstance()
                        .sendEachForMulticastAsync(message);

                CompletableFuture.supplyAsync(() -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        log.error("Firebase notification failed to send with error: {}", e.getMessage());
                        throw new RuntimeException("Firebase notification failed to send, e");
                    }
                }).thenAccept(batchResponse -> {
                    log.info("Notifications sent: {} success, {} failure",
                            batchResponse.getSuccessCount(), batchResponse.getFailureCount());
                    processBatchResponse(tokensToSendTo, batchResponse);
                }).exceptionally(e -> {
                    log.error("Error in Firebase async messaging", e);
                    return null;
                });

            } catch (Exception e){
                log.error("Error in Firebase Messaging: " + e.getMessage());
            }
        }
    }


    private void processBatchResponse(List<String> tokensToSendTo, BatchResponse batchResponse) {
        for(int i = 0; i < batchResponse.getResponses().size(); i++) {
            SendResponse sendResponse = batchResponse.getResponses().get(i);
            String token = tokensToSendTo.get(i);

            if(!sendResponse.isSuccessful()) {
                FirebaseMessagingException ex = (FirebaseMessagingException) sendResponse.getException();
                log.warn("Failed token: {}, reason: {}", token, ex.getMessagingErrorCode());

                if (ex.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                        || ex.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                    handleInvalidToken(token);
                    log.warn("Deactivated invalid token: {}", token);
                }
            }
        }
    }

    private void handleInvalidToken(String token) {
        tokenService.invalidateToken(token);
    }
}