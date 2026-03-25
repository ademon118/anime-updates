package com.aura.anime_updates.features.animeShow.api;

import com.aura.anime_updates.features.animeShow.api.response.AnimeShowResponse;
import com.aura.anime_updates.features.animeShow.domain.service.AnimeShowService;
import com.aura.anime_updates.features.release.api.response.ReleaseInfoResponse;
import com.aura.anime_updates.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anime-show")
@RequiredArgsConstructor
@Tag(name = "Anime Show", description = "APIs for managing anime shows and their releases")
public class AnimeShowApiController {

    private final AnimeShowService animeShowService;

    @Operation(summary = "Get anime show releases", description = "Retrieve paginated list of releases for a specific anime show")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Releases retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Anime show not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @GetMapping("/{animeShowId}/get-releases")
    public ResponseEntity<Page<ReleaseInfoResponse>> getReleaseLinks(
            @Parameter(description = "ID of the anime show", required = true)
            @PathVariable Long animeShowId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0")  Integer page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "0") Integer size,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ){
        Long userId = null;
        if(currentUser != null) {
            userId = currentUser.getId();
        }
        return ResponseEntity.ok(animeShowService.getAllReleaseInfoOfAnAnimeShow(page, size, animeShowId, userId));
    }

    @Operation(summary = "Search anime shows", description = "Retrieve paginated list of searched shows")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shows retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Anime shows not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @GetMapping("/{searchText}")
    public ResponseEntity<Page<AnimeShowResponse>> searchAnimeShows(
            @Parameter(description = "Search string")
            @PathVariable String searchText,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0")  Integer page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") Integer size
    ){
        return ResponseEntity.ok(animeShowService.searchAnimeShows(page, size, searchText));
    }
}
