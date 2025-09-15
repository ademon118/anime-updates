package com.aura.anime_updates.features.tracking.api;

import com.aura.anime_updates.features.release.api.response.ReleaseInfoResponse;
import com.aura.anime_updates.features.release.domain.service.ReleaseService;
import com.aura.anime_updates.features.tracking.domain.service.TrackingService;
import com.aura.anime_updates.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@Tag(name = "Tracking", description = "APIs for tracking anime shows and managing tracked releases")
public class TrackedReleasesApiController {

    private final ReleaseService releaseService;
    private final TrackingService trackingService;

    @Operation(summary = "Get tracked releases", description = "Retrieve paginated list of releases for tracked anime shows")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tracked releases retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - valid access token required")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/get-releases")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ReleaseInfoResponse>> getAllTrackedReleases(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0")  Integer page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(releaseService.getAllTrackedReleaseInfo(page, size, currentUser.getId()));
    }

    @Operation(summary = "Track anime show", description = "Start tracking a specific anime show for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anime show tracked successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - valid access token required"),
            @ApiResponse(responseCode = "404", description = "Anime show not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/track/{animeShowId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> trackAnimeShow(
            @Parameter(description = "ID of the anime show to track", required = true)
            @PathVariable Long animeShowId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        trackingService.trackAnimeShow(currentUser.getId(), animeShowId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Untrack anime show", description = "Stop tracking a specific anime show for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anime show untracked successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - valid access token required"),
            @ApiResponse(responseCode = "404", description = "Anime show not found or not being tracked")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/untrack/{animeShowId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unTrackAnimeShow(
            @Parameter(description = "ID of the anime show to untrack", required = true)
            @PathVariable Long animeShowId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        trackingService.unTrackAnimeShow(currentUser.getId(), animeShowId);
        return ResponseEntity.ok().build();
    }

}