package com.aura.anime_updates.security;

import com.aura.anime_updates.features.watchparty.domain.service.CleanupService;
import com.aura.anime_updates.features.watchparty.domain.service.WatchPartyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketSecurityInterceptor implements ChannelInterceptor {

    private final WatchPartyManager watchPartyManager;
    private final CleanupService cleanupService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            handleConnect(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            handleSubscribe(accessor);
        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            handleDisconnect(accessor);
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        CustomUserDetails user = (CustomUserDetails) accessor.getSessionAttributes().get("user");
        if (user == null) {
            throw new IllegalArgumentException("Unauthorized WebSocket connection");
        }

        String partyId = accessor.getFirstNativeHeader("partyId");
        if (partyId == null || partyId.isBlank()) {
            throw new IllegalArgumentException("partyId header is required");
        }

        String userId = String.valueOf(user.getId());
        if (!watchPartyManager.isMember(partyId, userId)) {
            throw new IllegalArgumentException("Party not found or access denied");
        }

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        sessionAttributes.put("partyId", partyId);
        sessionAttributes.put("userId", userId);
        sessionAttributes.put("username", user.getUsername());

        accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        cleanupService.cancelGracePeriod(partyId, userId);
        watchPartyManager.getParty(partyId).ifPresent(party -> party.getActiveMembers().add(userId));
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith("/topic/party/")) {
            return;
        }

        String partyId = destination.substring("/topic/party/".length());
        String userId = (String) accessor.getSessionAttributes().get("userId");

        if (userId == null || !watchPartyManager.isMember(partyId, userId)) {
            throw new IllegalArgumentException("Party not found or access denied");
        }
    }

    private void handleDisconnect(StompHeaderAccessor accessor) {
        String partyId = (String) accessor.getSessionAttributes().get("partyId");
        String userId = (String) accessor.getSessionAttributes().get("userId");

        if (partyId != null && userId != null) {
            watchPartyManager.getParty(partyId).ifPresent(party -> party.getActiveMembers().remove(userId));
        }
    }
}
