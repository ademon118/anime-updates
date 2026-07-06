package com.aura.anime_updates.features.friends.api;

import com.aura.anime_updates.features.friends.api.request.FriendRequestDTO;
import com.aura.anime_updates.features.friends.api.response.FriendResponseDTO;
import com.aura.anime_updates.features.friends.domain.service.FriendService;
import com.aura.anime_updates.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@Tag(name = "Friends", description = "APIs for sending, accepting, and managing friend requests")
public class FriendshipController {

    private final FriendService friendService;

    @Operation(summary = "Send friend request", description = "Send a friend request to another user by username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request sent successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - valid access token required"),
            @ApiResponse(responseCode = "404", description = "Target user not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g. sending request to yourself)"),
            @ApiResponse(responseCode = "409", description = "Friend request already exists or users are already friends")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/request")
    public ResponseEntity<String> sendFriendRequest(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody FriendRequestDTO request) {
        friendService.sendRequest(currentUser.getId(), request.username());
        return ResponseEntity.ok("Friend request sent successfully.");
    }

    @Operation(summary = "Accept friend request", description = "Accept a pending friend request from another user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request accepted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - valid access token required"),
            @ApiResponse(responseCode = "404", description = "User or pending request not found"),
            @ApiResponse(responseCode = "400", description = "Request was not sent by the specified user")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/accept")
    public ResponseEntity<String> acceptRequest(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody FriendRequestDTO request) {
        friendService.accept(currentUser.getId(), request.username());
        return ResponseEntity.ok("Friend request accepted.");
    }

    @Operation(summary = "Decline friend request", description = "Decline a pending friend request from another user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request declined"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - valid access token required"),
            @ApiResponse(responseCode = "404", description = "User or pending request not found"),
            @ApiResponse(responseCode = "400", description = "Request was not sent by the specified user")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/decline")
    public ResponseEntity<String> declineRequest(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody FriendRequestDTO request) {
        friendService.decline(currentUser.getId(), request.username());
        return ResponseEntity.ok("Friend request declined.");
    }

    @Operation(summary = "Remove friend or request", description = "Remove an existing friendship or cancel a pending friend request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend or request removed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - valid access token required"),
            @ApiResponse(responseCode = "404", description = "User or friendship not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/remove")
    public ResponseEntity<String> removeFriend(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody FriendRequestDTO request) {
        friendService.remove(currentUser.getId(), request.username());
        return ResponseEntity.ok("Friend/Request removed successfully.");
    }

    @Operation(summary = "Get my friends", description = "Retrieve the authenticated user's friends and pending requests")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend list retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - valid access token required")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    public ResponseEntity<List<FriendResponseDTO>> getMyFriends(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(friendService.getFriendList(currentUser.getId()));
    }
}
