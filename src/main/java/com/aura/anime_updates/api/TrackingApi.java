package com.aura.anime_updates.api;

import com.aura.anime_updates.domain.AnimeShow;
import com.aura.anime_updates.domain.Release;
import com.aura.anime_updates.dto.ApiResponse;
import com.aura.anime_updates.services.TrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/tracking")
public class TrackingApi {

    private final TrackingService trackingService;

    public TrackingApi(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping("/{userId}/track/{showId}")
    public ResponseEntity<ApiResponse<String>> track(@PathVariable long userId, @PathVariable long showId) {
        trackingService.trackShow(userId, showId);
        return ResponseEntity.ok(ApiResponse.success("Show tracked"));
    }

    @PostMapping("/{userId}/untrack/{showId}")
    public ResponseEntity<ApiResponse<String>> untrack(@PathVariable long userId, @PathVariable long showId) {
        trackingService.untrackShow(userId, showId);
        return ResponseEntity.ok(ApiResponse.success("Show untracked"));
    }

    @GetMapping("/{userId}/shows")
    public ResponseEntity<ApiResponse<Set<AnimeShow>>> getTrackedShows(@PathVariable long userId) {
        return ResponseEntity.ok(ApiResponse.success(trackingService.getTrackedShows(userId), "Tracked shows"));
    }

    @GetMapping("/{userId}/releases")
    public ResponseEntity<ApiResponse<List<Release>>> getTrackedReleases(@PathVariable long userId) {
        return ResponseEntity.ok(ApiResponse.success(trackingService.getTrackedReleases(userId), "Tracked releases"));
    }
}


