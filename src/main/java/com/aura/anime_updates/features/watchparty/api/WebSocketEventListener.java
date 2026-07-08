package com.aura.anime_updates.features.watchparty.api;

import com.aura.anime_updates.features.watchparty.domain.service.WatchPartyMembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final WatchPartyMembershipService membershipService;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = headers.getSessionAttributes();
        if (sessionAttributes == null) {
            return;
        }

        String partyId = (String) sessionAttributes.get("partyId");
        String userId = (String) sessionAttributes.get("userId");
        if (partyId != null && userId != null) {
            membershipService.markOffline(partyId, userId);
        }
    }
}
