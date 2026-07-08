package com.aura.anime_updates.features.watchparty.domain.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class WatchPartyException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public WatchPartyException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static WatchPartyException partyNotFound() {
        return new WatchPartyException(HttpStatus.NOT_FOUND, "party_not_found", "Watch party not found.");
    }

    public static WatchPartyException notFriends() {
        return new WatchPartyException(HttpStatus.FORBIDDEN, "not_friends", "You can only invite or join friends.");
    }

    public static WatchPartyException invalidToken() {
        return new WatchPartyException(HttpStatus.FORBIDDEN, "invalid_invite_token", "Invalid or expired invite token.");
    }

    public static WatchPartyException notMember() {
        return new WatchPartyException(HttpStatus.FORBIDDEN, "not_party_member", "You are not a member of this party.");
    }

    public static WatchPartyException selfInvite() {
        return new WatchPartyException(HttpStatus.BAD_REQUEST, "self_invite", "You cannot invite yourself.");
    }

    public static WatchPartyException notInvitee() {
        return new WatchPartyException(HttpStatus.FORBIDDEN, "not_invitee", "This invite was not sent to you.");
    }

    public static WatchPartyException leaderOnly() {
        return new WatchPartyException(HttpStatus.FORBIDDEN, "leader_only", "Only the party creator can load a video.");
    }

    public static WatchPartyException alreadyMember() {
        return new WatchPartyException(HttpStatus.CONFLICT, "already_member", "That friend is already in the party.");
    }
}
