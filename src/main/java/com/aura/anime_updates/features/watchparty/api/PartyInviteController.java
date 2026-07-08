package com.aura.anime_updates.features.watchparty.api;

import com.aura.anime_updates.dto.ApiResponse;
import com.aura.anime_updates.features.watchparty.api.request.AcceptInviteRequest;
import com.aura.anime_updates.features.watchparty.api.request.DeclineInviteRequest;
import com.aura.anime_updates.features.watchparty.api.request.InviteRequest;
import com.aura.anime_updates.features.watchparty.api.response.PartyInviteResponse;
import com.aura.anime_updates.features.watchparty.api.response.PartyStateResponse;
import com.aura.anime_updates.features.watchparty.domain.exceptions.WatchPartyException;
import com.aura.anime_updates.features.watchparty.domain.service.WatchPartyService;
import com.aura.anime_updates.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/party")
@RequiredArgsConstructor
@Tag(name = "Watch Party", description = "APIs for creating and joining watch parties with friends")
public class PartyInviteController {

    private final WatchPartyService watchPartyService;

    @Operation(
            summary = "Invite a friend to a watch party",
            description = "Creates a party when the caller has none, or adds a pending invite to the caller's existing party"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/invite")
    public ResponseEntity<ApiResponse<PartyInviteResponse>> inviteFriend(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody InviteRequest request) {
        PartyInviteResponse response = watchPartyService.inviteFriend(
                requireUsername(currentUser),
                request.friendUsername()
        );
        return ResponseEntity.ok(ApiResponse.success(response, "Invite created successfully."));
    }

    @Operation(summary = "Accept a watch party invite", description = "Join a party using the invite token from the party leader")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{partyId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptInvite(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable String partyId,
            @RequestBody AcceptInviteRequest request) {
        watchPartyService.acceptInvite(requireUsername(currentUser), partyId, request.token());
        return ResponseEntity.ok(ApiResponse.success("Joined watch party successfully."));
    }

    @Operation(summary = "Decline a watch party invite", description = "Reject a party invite; frees the party if the leader has no other invites or members")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{partyId}/decline")
    public ResponseEntity<ApiResponse<Void>> declineInvite(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable String partyId,
            @RequestBody DeclineInviteRequest request) {
        watchPartyService.declineInvite(requireUsername(currentUser), partyId, request.token());
        return ResponseEntity.ok(ApiResponse.success("Invite declined."));
    }

    @Operation(summary = "Get watch party state", description = "Returns the current playback state for a party the user belongs to")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{partyId}")
    public ResponseEntity<ApiResponse<PartyStateResponse>> getPartyState(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable String partyId) {
        PartyStateResponse state = watchPartyService.getPartyState(partyId, requireUsername(currentUser));
        return ResponseEntity.ok(ApiResponse.success(state, "Party state retrieved successfully."));
    }

    private String requireUsername(CustomUserDetails currentUser) {
        if (currentUser == null || !StringUtils.hasText(currentUser.getUsername())) {
            throw WatchPartyException.missingUsername();
        }
        return currentUser.getUsername().trim();
    }
}
