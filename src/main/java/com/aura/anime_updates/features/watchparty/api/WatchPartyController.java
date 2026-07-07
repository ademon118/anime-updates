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
        CustomUserDetails resolvedSender = resolveSender(sender, headerAccessor);
        if (resolvedSender == null) {
            log.warn(
                    "Watch party sync dropped: no authenticated sender partyId={} action={}",
                    partyId,
                    action.action()
            );
            return null;
        }

        String userId = String.valueOf(resolvedSender.getId());
        WatchParty party = watchPartyManager.getParty(partyId).orElse(null);
        if (party == null || !party.getJoinedMembers().contains(userId)) {
            log.warn(
                    "Watch party sync dropped: party missing or user not member partyId={} userId={} action={}",
                    partyId,
                    userId,
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

        String senderName = resolveSenderName(resolvedSender);
        SyncAction broadcast = applyAction(party, action, senderName);
        party.setLastUpdated(System.currentTimeMillis());
        log.info(
                "Watch party broadcast partyId={} action={} from={} videoUrl={}",
                partyId,
                broadcast.action(),
                senderName,
                broadcast.videoUrl()
        );
        return broadcast;
    }

    private CustomUserDetails resolveSender(
            CustomUserDetails sender,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        if (sender != null) {
            return sender;
        }

        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return null;
        }

        Object sessionUser = sessionAttributes.get("user");
        if (sessionUser instanceof CustomUserDetails details) {
            return details;
        }

        return null;
    }

    private String resolveSenderName(CustomUserDetails sender) {
        String username = sender.getUsername();
        if (username == null || username.isBlank()) {
            return String.valueOf(sender.getId());
        }
        return username;
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
