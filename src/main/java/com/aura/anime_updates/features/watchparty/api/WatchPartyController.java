package com.aura.anime_updates.features.watchparty.api;

import com.aura.anime_updates.features.watchparty.domain.entity.WatchParty;
import com.aura.anime_updates.features.watchparty.domain.service.WatchPartyManager;
import com.aura.anime_updates.features.watchparty.enums.SyncActionType;
import com.aura.anime_updates.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WatchPartyController {

    private final WatchPartyManager watchPartyManager;

    @MessageMapping("/party/{partyId}/sync")
    @SendTo("/topic/party/{partyId}")
    public SyncAction handleSync(@DestinationVariable String partyId,
                                 SyncAction action,
                                 @AuthenticationPrincipal CustomUserDetails sender) {
        if (sender == null) {
            return null;
        }

        String userId = String.valueOf(sender.getId());
        WatchParty party = watchPartyManager.getParty(partyId).orElse(null);
        if (party == null || !party.getJoinedMembers().contains(userId)) {
            return null;
        }

        if (action.action() == SyncActionType.LOAD_VIDEO && !party.getLeaderId().equals(userId)) {
            return null;
        }

        SyncAction broadcast = applyAction(party, action, sender.getUsername());
        party.setLastUpdated(System.currentTimeMillis());
        return broadcast;
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
