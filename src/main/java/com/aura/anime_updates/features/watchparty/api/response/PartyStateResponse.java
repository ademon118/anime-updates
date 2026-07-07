package com.aura.anime_updates.features.watchparty.api.response;

import lombok.Builder;

import java.util.Set;

@Builder
public record PartyStateResponse(
        String partyId,
        String leaderId,
        String videoUrl,
        double currentTimeStamp,
        boolean isPlaying,
        Set<String> members,
        Set<String> activeMembers
) {
}
