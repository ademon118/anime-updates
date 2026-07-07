package com.aura.anime_updates.features.watchparty.api;

import com.aura.anime_updates.features.watchparty.domain.entity.WatchParty;
import com.aura.anime_updates.features.watchparty.domain.service.WatchPartyManager;
import com.aura.anime_updates.features.watchparty.enums.SyncActionType;
import com.aura.anime_updates.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WatchPartyController {

    private final WatchPartyManager watchPartyManager;

    @MessageMapping("/party/{partyId}/sync")
    @SendTo("/topic/party/{partyId}")
    public SyncAction handleSync(@DestinationVariable String partyId,
                                 SyncAction action,
                                 @AuthenticationPrincipal CustomUserDetails sender,
                                 SimpMessageHeaderAccessor headerAccessor) {
        String userId = resolveUserId(sender, headerAccessor);
        if (userId == null) {
            log.warn(
                    "Watch party sync dropped: no userId partyId={} action={} principal={}",
                    partyId,
                    action.action(),
                    sender
            );
            return null;
        }

        WatchParty party = watchPartyManager.getParty(partyId).orElse(null);
        if (party == null) {
            log.warn(
                    "Watch party sync dropped: party not found partyId={} userId={} action={}",
                    partyId,
                    userId,
                    action.action()
            );
            return null;
        }

        if (!party.getJoinedMembers().contains(userId)) {
            log.warn(
                    "Watch party sync dropped: user not member partyId={} userId={} members={} action={}",
                    partyId,
                    userId,
                    party.getJoinedMembers(),
                    action.action()
            );
            return null;
        }

        if (action.action() == SyncActionType.LOAD_VIDEO && !party.getLeaderId().equals(userId)) {
            log.warn(
                    "Watch party sync dropped: non-leader LOAD_VIDEO partyId={} userId={}",
                    partyId,
                    userId
            );
            return null;
        }

        String senderName = resolveSenderName(userId, headerAccessor, sender);
        SyncAction broadcast = applyAction(party, action, senderName);
        party.setLastUpdated(System.currentTimeMillis());
        log.info(
                "Watch party broadcast partyId={} action={} from={} userId={} videoUrl={}",
                partyId,
                broadcast.action(),
                senderName,
                userId,
                broadcast.videoUrl()
        );
        return broadcast;
    }

    /**
     * Session userId is set on CONNECT and is the most reliable identity for STOMP SEND.
     */
    private String resolveUserId(CustomUserDetails sender, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Object sessionUserId = sessionAttributes.get("userId");
            if (sessionUserId != null && !sessionUserId.toString().isBlank()) {
                return sessionUserId.toString().trim();
            }

            Object sessionUser = sessionAttributes.get("user");
            if (sessionUser instanceof CustomUserDetails details && details.getId() != null) {
                return String.valueOf(details.getId());
            }
        }

        if (sender != null && sender.getId() != null) {
            return String.valueOf(sender.getId());
        }

        return null;
    }

    private String resolveSenderName(
            String userId,
            SimpMessageHeaderAccessor headerAccessor,
            CustomUserDetails sender
    ) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Object sessionUsername = sessionAttributes.get("username");
            if (sessionUsername != null && !sessionUsername.toString().isBlank()) {
                return sessionUsername.toString().trim();
            }
        }

        if (sender != null) {
            String username = sender.getUsername();
            if (username != null && !username.isBlank()) {
                return username;
            }
        }

        return userId;
    }

    private SyncAction applyAction(WatchParty party, SyncAction action, String senderUsername) {
        return switch (action.action()) {
            case PLAY -> {
                party.setPlaying(true);
                yield action.withSender(senderUsername);
            }
            case PAUSE -> {
                party.setPlaying(false);
                yield action.withSender(senderUsername);
            }
            case SEEK -> {
                party.setCurrentTimeStamp(action.timestamp());
                yield action.withSender(senderUsername);
            }
            case LOAD_VIDEO -> {
                party.setVideoUrl(action.videoUrl());
                party.setCurrentTimeStamp(0.0);
                party.setPlaying(false);
                yield action.withSender(senderUsername);
            }
            case SYNC_REQUEST -> SyncAction.builder()
                    .action(SyncActionType.SYNC_REQUEST)
                    .timestamp(party.getCurrentTimeStamp())
                    .isPlaying(party.isPlaying())
                    .videoUrl(party.getVideoUrl())
                    .senderUsername(senderUsername)
                    .leaderId(party.getLeaderId())
                    .build();
            case JOIN, LEAVE, LEADER_CHANGE -> action.withSender(senderUsername);
        };
    }
}
