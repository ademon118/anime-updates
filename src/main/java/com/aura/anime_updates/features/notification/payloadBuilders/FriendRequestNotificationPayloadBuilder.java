package com.aura.anime_updates.features.notification.payloadBuilders;

import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FriendRequestNotificationPayloadBuilder {

    public Notification buildFriendRequestNotificationPayload(String senderUsername) {
        return Notification.builder()
                .setTitle(senderUsername)
                .setBody("wants to be your tomodachi")
                .build();
    }

    public Notification buildFriendRequestAcceptedNotificationPayload(String accepterUsername) {
        return Notification.builder()
                .setTitle(accepterUsername)
                .setBody("accepted your tomodachi request")
                .build();
    }
}
