package com.aura.anime_updates.api;

import com.aura.anime_updates.dto.ApiResponse;
import com.aura.anime_updates.dto.TrackedReleaseDto;
import com.aura.anime_updates.dto.TrackedShowDto;
import com.aura.anime_updates.services.TrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/tracking")
public class TrackingApi {

    private static final Logger logger = LoggerFactory.getLogger(TrackingApi.class);

    private final TrackingService trackingService;

    public TrackingApi(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping("/{userId}/track/{showId}")
    public ResponseEntity<ApiResponse<String>> track(@PathVariable long userId, @PathVariable long showId) {
        logger.info("API: Tracking show {} for user {}", showId, userId);
        trackingService.trackShow(userId, showId);
        return ResponseEntity.ok(ApiResponse.success("Show tracked"));
    }

    @PostMapping("/{userId}/untrack/{showId}")
    public ResponseEntity<ApiResponse<String>> untrack(@PathVariable long userId, @PathVariable long showId) {
        logger.info("API: Untracking show {} for user {}", showId, userId);
        trackingService.untrackShow(userId, showId);
        return ResponseEntity.ok(ApiResponse.success("Show untracked"));
    }

    @GetMapping("/{userId}/shows")
    public ResponseEntity<ApiResponse<Page<TrackedShowDto>>> getTrackedShows(
            @PathVariable long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ) {
        Pageable pageable = PageRequest.of(page,size);
        Page<TrackedShowDto> shows = trackingService.getTrackedShows(userId,pageable);
        return ResponseEntity.ok(ApiResponse.success(shows, "Tracked shows"));
    }

    @GetMapping("/{userId}/releases")
    public ResponseEntity<ApiResponse<Page<TrackedReleaseDto>>> getTrackedReleases(
            @PathVariable long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page,size);
        Page<TrackedReleaseDto> releases = trackingService.getTrackedReleases(userId,pageable);
        return ResponseEntity.ok(ApiResponse.success(releases, "Tracked releases"));
    }
}


