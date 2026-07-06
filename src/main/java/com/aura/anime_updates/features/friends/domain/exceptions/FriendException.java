package com.aura.anime_updates.features.friends.domain.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class FriendException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public FriendException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static FriendException usernameRequired() {
        return new FriendException(HttpStatus.BAD_REQUEST, "username_required", "Username is required.");
    }

    public static FriendException userNotFound(String identifier) {
        return new FriendException(HttpStatus.NOT_FOUND, "user_not_found", "User not found: " + identifier);
    }

    public static FriendException selfRequest() {
        return new FriendException(
                HttpStatus.BAD_REQUEST,
                "self_friend_request",
                "You cannot send a friend request to yourself."
        );
    }

    public static FriendException alreadyExists() {
        return new FriendException(
                HttpStatus.CONFLICT,
                "friendship_already_exists",
                "A friend request already exists or you are already friends."
        );
    }

    public static FriendException friendshipNotFound() {
        return new FriendException(
                HttpStatus.NOT_FOUND,
                "friendship_not_found",
                "Friendship or request does not exist."
        );
    }

    public static FriendException pendingRequestNotFound(String username) {
        return new FriendException(
                HttpStatus.NOT_FOUND,
                "pending_request_not_found",
                "No pending request from " + username
        );
    }

    public static FriendException invalidRequestSender(String username) {
        return new FriendException(
                HttpStatus.BAD_REQUEST,
                "invalid_friend_request",
                "This request was not sent by " + username
        );
    }
}
