package com.aura.anime_updates.features.friends.api;

import com.aura.anime_updates.features.friends.api.request.FriendRequestDTO;
import com.aura.anime_updates.features.friends.api.response.FriendResponseDTO;
import com.aura.anime_updates.features.friends.domain.service.FriendService;
import com.aura.anime_updates.features.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendService friendService;

    @PostMapping("/request")
    public ResponseEntity<String> sendFriendRequest(
            @AuthenticationPrincipal User currentUser,
            @RequestBody FriendRequestDTO request) {
        friendService.sendRequest(currentUser, request.username());
        return ResponseEntity.ok("Friend request sent successfully.");
    }

    @PostMapping("/accept")
    public ResponseEntity<String> acceptRequest(
            @AuthenticationPrincipal User currentUser,
            @RequestBody FriendRequestDTO request) {
        friendService.accept(currentUser, request.username());
        return ResponseEntity.ok("Friend request accepted.");
    }

    @PostMapping("/decline")
    public ResponseEntity<String> declineRequest(
            @AuthenticationPrincipal User currentUser,
            @RequestBody FriendRequestDTO request) {
        friendService.decline(currentUser, request.username());
        return ResponseEntity.ok("Friend request declined.");
    }

    @PostMapping("/remove")
    public ResponseEntity<String> removeFriend(
            @AuthenticationPrincipal User currentUser,
            @RequestBody FriendRequestDTO request) {
        friendService.remove(currentUser, request.username());
        return ResponseEntity.ok("Friend/Request removed successfully.");
    }

    @GetMapping
    public ResponseEntity<List<FriendResponseDTO>> getMyFriends(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(friendService.getFriendList(currentUser));
    }
}