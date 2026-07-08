package com.aura.anime_updates.features.watchparty.domain.service;

import com.aura.anime_updates.features.watchparty.api.SyncAction;

import java.util.Optional;

public record MemberLeaveResult(
        String partyId,
        SyncAction leaveBroadcast,
        Optional<SyncAction> leaderChangeBroadcast,
        boolean partyDissolved
) {
    public static MemberLeaveResult dissolved(String partyId, SyncAction leaveBroadcast) {
        return new MemberLeaveResult(partyId, leaveBroadcast, Optional.empty(), true);
    }

    public static MemberLeaveResult remaining(
            String partyId,
            SyncAction leaveBroadcast,
            Optional<SyncAction> leaderChangeBroadcast
    ) {
        return new MemberLeaveResult(partyId, leaveBroadcast, leaderChangeBroadcast, false);
    }
}
