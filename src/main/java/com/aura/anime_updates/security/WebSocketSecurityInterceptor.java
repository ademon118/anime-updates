package com.aura.anime_updates.security;

import com.aura.anime_updates.features.watchparty.domain.entity.WatchParty;
import com.aura.anime_updates.features.watchparty.domain.service.WatchPartyManager;
import com.aura.anime_updates.features.watchparty.domain.service.WatchPartyMembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSecurityInterceptor implements ChannelInterceptor {

    private final WatchPartyManager watchPartyManager;
    private final WatchPartyMembershipService membershipService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        try {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                handleConnect(accessor);
            } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                handleSubscribe(accessor);
            } else if (StompCommand.SEND.equals(accessor.getCommand())) {
                ensureSessionUser(accessor);
            }
        } catch (MessageDeliveryException ex) {
            log.warn("Watch party WebSocket rejected: {}", ex.getMessage());
            throw ex;
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        CustomUserDetails user = resolveUser(accessor);
        if (user == null) {
            reject("Unauthorized WebSocket connection");
        }

        String partyId = resolvePartyId(accessor);
        if (partyId == null || partyId.isBlank()) {
            reject("partyId is required");
        }

        String username = resolveUsername(user);
        assertJoinedMember(partyId, username, "CONNECT");

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            reject("WebSocket session is unavailable");
        }

        sessionAttributes.put("partyId", partyId);
        sessionAttributes.put("username", username);
        sessionAttributes.put("user", user);

        accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        membershipService.markOnline(partyId, username);
        log.info("Watch party WebSocket connected: partyId={} username={}", partyId, username);
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith("/topic/party/")) {
            return;
        }

        String partyId = destination.substring("/topic/party/".length());
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        String username = sessionAttributes != null ? (String) sessionAttributes.get("username") : null;

        if (username == null) {
            log.warn("Watch party SUBSCRIBE missing session username: partyId={} destination={}", partyId, destination);
            reject("WebSocket session is not established");
        }

        assertJoinedMember(partyId, username, "SUBSCRIBE");
    }

    private void ensureSessionUser(StompHeaderAccessor accessor) {
        CustomUserDetails user = resolveUser(accessor);
        if (user == null) {
            log.warn("Watch party SEND missing authenticated user");
            reject("Unauthorized WebSocket message");
        }

        accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null && sessionAttributes.get("username") == null) {
            sessionAttributes.put("username", resolveUsername(user));
            sessionAttributes.put("user", user);
        }
    }

    private void assertJoinedMember(String partyId, String username, String phase) {
        Optional<WatchParty> partyOpt = watchPartyManager.getParty(partyId);
        if (partyOpt.isEmpty()) {
            log.warn("Watch party {} rejected, party not found: partyId={} username={}", phase, partyId, username);
            reject("Party not found");
        }

        WatchParty party = partyOpt.get();
        if (!party.getJoinedMembers().contains(username)) {
            log.warn(
                    "Watch party {} rejected, not a member: partyId={} username={} members={} leaderUsername={}",
                    phase,
                    partyId,
                    username,
                    party.getJoinedMembers(),
                    party.getLeaderUsername()
            );
            reject("Access denied");
        }
    }

    private CustomUserDetails resolveUser(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Object user = sessionAttributes.get("user");
            if (user instanceof CustomUserDetails details) {
                return details;
            }
        }

        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details;
        }

        return null;
    }

    private String resolvePartyId(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Object value = sessionAttributes.get("partyId");
            if (value != null && !value.toString().isBlank()) {
                return value.toString().trim();
            }
        }

        String partyId = accessor.getFirstNativeHeader("partyId");
        if (partyId != null && !partyId.isBlank()) {
            return partyId.trim();
        }

        return null;
    }

    private String resolveUsername(CustomUserDetails user) {
        String username = user.getUsername();
        if (username == null || username.isBlank()) {
            reject("Account username is required for watch party");
        }
        return username.trim();
    }

    private void reject(String reason) {
        throw new MessageDeliveryException(reason);
    }
}
