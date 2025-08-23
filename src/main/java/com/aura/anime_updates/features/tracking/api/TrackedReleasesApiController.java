package com.aura.anime_updates.features.tracking.api;

import com.aura.anime_updates.features.release.api.response.ReleaseInfoResponse;
import com.aura.anime_updates.features.release.domain.service.ReleaseService;
import com.aura.anime_updates.features.tracking.domain.service.TrackingService;
import com.aura.anime_updates.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackedReleasesApiController {

    private final ReleaseService releaseService;
    private final TrackingService trackingService;

    @GetMapping("/get-releases")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ReleaseInfoResponse>> getAllTrackedReleases(
            @RequestParam(defaultValue = "0")  Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(releaseService.getAllTrackedReleaseInfo(page, size, currentUser.getId()));
    }

    @PostMapping("/track/{animeShowId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> trackAnimeShow(
            @PathVariable Long animeShowId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        trackingService.trackAnimeShow(currentUser.getId(), animeShowId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/untrack/{animeShowId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unTrackAnimeShow(
            @PathVariable Long animeShowId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        trackingService.unTrackAnimeShow(currentUser.getId(), animeShowId);
        return ResponseEntity.ok().build();
    }

}