package com.aura.anime_updates.features.watchparty.api;

import com.aura.anime_updates.features.watchparty.domain.entity.WatchParty;
import com.aura.anime_updates.features.watchparty.domain.service.WatchPartyManager;
import com.aura.anime_updates.features.watchparty.domain.service.WatchPartyMembershipService;
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
import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WatchPartyController {

    private final WatchPartyManager watchPartyManager;
    private final WatchPartyMembershipService membershipService;

    @MessageMapping("/party/{partyId}/sync")
    @SendTo("/topic/party/{partyId}")
    public SyncAction handleSync(@DestinationVariable String partyId,
                                 SyncAction action,
                                 @AuthenticationPrincipal CustomUserDetails sender,
                                 SimpMessageHeaderAccessor headerAccessor) {
        String username = resolveUsername(sender, headerAccessor);
        if (username == null) {
            log.warn(
                    "Watch party sync dropped: no username partyId={} action={} principal={}",
                    partyId,
                    action.action(),
                    sender
            );
            return null;
        }

        WatchParty party = watchPartyManager.getParty(partyId).orElse(null);
        if (party == null) {
            log.warn(
                    "Watch party sync dropped: party not found partyId={} username={} action={}",
                    partyId,
                    username,
                    action.action()
            );
            return null;
        }

        if (!party.getJoinedMembers().contains(username)) {
            log.warn(
                    "Watch party sync dropped: user not member partyId={} username={} members={} action={}",
                    partyId,
                    username,
                    party.getJoinedMembers(),
                    action.action()
            );
            return null;
        }

        if (action.action() == SyncActionType.LOAD_VIDEO && !party.getLeaderUsername().equals(username)) {
            log.warn(
                    "Watch party sync dropped: non-leader LOAD_VIDEO partyId={} username={}",
                    partyId,
                    username
            );
            return null;
        }

        if (action.action() == SyncActionType.LEAVE) {
            return membershipService.leaveExplicitly(partyId, username).orElse(null);
        }

        if (action.action() == SyncActionType.PRESENCE) {
            log.warn(
                    "Watch party sync dropped: client PRESENCE partyId={} username={}",
                    partyId,
                    username
            );
            return null;
        }

        if (action.action() == SyncActionType.HEARTBEAT) {
            return null;
        }

        if (action.action() == SyncActionType.JOIN) {
            // Membership JOIN is broadcast from acceptInvite; client JOIN is presence-only.
            return null;
        }

        SyncAction broadcast = applyAction(party, action, username);
        party.setLastUpdated(System.currentTimeMillis());
        log.info(
                "Watch party broadcast partyId={} action={} from={} videoUrl={}",
                partyId,
                broadcast.action(),
                username,
                broadcast.videoUrl()
        );
        return broadcast;
    }

    private String resolveUsername(CustomUserDetails sender, SimpMessageHeaderAccessor headerAccessor) {
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
                return username.trim();
            }
        }

        return null;
    }

    private SyncAction applyAction(WatchParty party, SyncAction action, String senderUsername) {
        return switch (action.action()) {
            case PLAY -> {
                party.setPlaying(true);
                party.setCurrentTimeStamp(action.timestamp());
                yield action.withSender(senderUsername);
            }
            case PAUSE -> {
                party.setPlaying(false);
                party.setCurrentTimeStamp(action.timestamp());
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
                    .leaderUsername(party.getLeaderUsername())
                    .build();
            case LEADER_CHANGE -> SyncAction.builder()
                    .action(action.action())
                    .senderUsername(senderUsername)
                    .leaderUsername(party.getLeaderUsername())
                    .members(Set.copyOf(party.getJoinedMembers()))
                    .activeMembers(Set.copyOf(party.getActiveMembers()))
                    .build();
            case JOIN -> throw new IllegalStateException("JOIN is broadcast from acceptInvite only");
            case LEAVE -> throw new IllegalStateException("LEAVE is handled by WatchPartyMembershipService");
            case PRESENCE -> throw new IllegalStateException("PRESENCE is server-only");
            case HEARTBEAT -> throw new IllegalStateException("HEARTBEAT is keepalive-only");
        };
    }
}
