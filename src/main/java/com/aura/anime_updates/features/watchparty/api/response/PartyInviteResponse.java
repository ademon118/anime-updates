package com.aura.anime_updates.features.watchparty.api.response;

import lombok.Builder;

@Builder
public record PartyInviteResponse(
        String partyId,
        String inviteToken
) {
}
