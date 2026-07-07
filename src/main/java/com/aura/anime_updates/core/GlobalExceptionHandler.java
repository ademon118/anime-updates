package com.aura.anime_updates.core;

import com.aura.anime_updates.features.authentication.domain.exceptions.InvalidCredentialsException;
import com.aura.anime_updates.features.friends.domain.exceptions.FriendException;
import com.aura.anime_updates.features.watchparty.domain.exceptions.WatchPartyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "invalid_credentials", ex.getMessage());
    }

    @ExceptionHandler(FriendException.class)
    public ResponseEntity<ErrorResponse> handleFriendException(FriendException ex) {
        return buildErrorResponse(ex.getStatus(), ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(WatchPartyException.class)
    public ResponseEntity<ErrorResponse> handleWatchPartyException(WatchPartyException ex) {
        return buildErrorResponse(ex.getStatus(), ex.getCode(), ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String code, String message) {
        ErrorResponse body = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .code(code)
                .message(message)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
