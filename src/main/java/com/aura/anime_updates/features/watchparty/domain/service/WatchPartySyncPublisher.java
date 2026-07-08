package com.aura.anime_updates.features.watchparty.domain.service;

import com.aura.anime_updates.features.watchparty.api.SyncAction;
import com.aura.anime_updates.features.watchparty.domain.entity.WatchParty;
import com.aura.anime_updates.features.watchparty.enums.SyncActionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Publishes watch-party sync events to the party WebSocket topic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WatchPartySyncPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishToParty(String partyId, SyncAction action) {
        messagingTemplate.convertAndSend("/topic/party/" + partyId, action);
        log.info("Watch party published {} partyId={}", action.action(), partyId);
    }

    public void publishMemberJoined(WatchParty party, String joinerUsername) {
        publishToParty(
                party.getPartyId(),
                SyncAction.builder()
                        .action(SyncActionType.JOIN)
                        .senderUsername(joinerUsername)
                        .leaderId(party.getLeaderId())
                        .members(Set.copyOf(party.getJoinedMembers()))
                        .build()
        );
    }

    public void publishPresence(String partyId, Set<String> activeMembers) {
        publishToParty(
                partyId,
                SyncAction.builder()
                        .action(SyncActionType.PRESENCE)
                        .activeMembers(activeMembers)
                        .build()
        );
    }

    public SyncAction buildLeaveBroadcast(
            String senderUsername,
            String leaderId,
            Set<String> remainingMembers
    ) {
        return SyncAction.builder()
                .action(SyncActionType.LEAVE)
                .senderUsername(senderUsername)
                .leaderId(leaderId)
                .members(remainingMembers)
                .build();
    }

    public SyncAction buildJoinBroadcast(
            String senderUsername,
            String leaderId,
            Set<String> members
    ) {
        return SyncAction.builder()
                .action(SyncActionType.JOIN)
                .senderUsername(senderUsername)
                .leaderId(leaderId)
                .members(members)
                .build();
    }
}
