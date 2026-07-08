package com.aura.anime_updates.features.notification.payloadBuilders;

import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WatchPartyNotificationPayloadBuilder {

    public static final String TYPE = "WATCH_PARTY_INVITE";
    public static final String DECLINED_TYPE = "WATCH_PARTY_DECLINED";

    public Notification buildInviteNotification(String leaderUsername) {
        return Notification.builder()
                .setTitle(leaderUsername)
                .setBody("invited you to a watch party")
                .build();
    }

    public Notification buildDeclineNotification(String friendUsername) {
        return Notification.builder()
                .setTitle(friendUsername)
                .setBody("declined your watch party invite")
                .build();
    }

    public Map<String, String> buildInviteData(String partyId, String inviteToken, String leaderId, String leaderUsername) {
        return Map.of(
                "type", TYPE,
                "partyId", partyId,
                "inviteToken", inviteToken,
                "leaderId", leaderId,
                "leaderUsername", leaderUsername
        );
    }

    public Map<String, String> buildDeclineData(
            String partyId,
            String declinedUserId,
            String declinedUsername
    ) {
        return Map.of(
                "type", DECLINED_TYPE,
                "partyId", partyId,
                "declinedUserId", declinedUserId,
                "declinedUsername", declinedUsername
        );
    }
}
