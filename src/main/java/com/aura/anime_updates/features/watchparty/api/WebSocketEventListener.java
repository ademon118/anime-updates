package com.aura.anime_updates.features.watchparty.api;

import com.aura.anime_updates.features.watchparty.domain.service.CleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final CleanupService cleanupService;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String partyId = (String) headers.getSessionAttributes().get("partyId");
        String userId = (String) headers.getSessionAttributes().get("userId");
        if (partyId != null && userId != null) {
            cleanupService.startGracePeriod(partyId, userId);
        }
    }
}
