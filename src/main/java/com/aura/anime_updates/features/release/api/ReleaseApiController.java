package com.aura.anime_updates.features.release.api;

import com.aura.anime_updates.features.release.api.response.ReleaseInfoResponse;
import com.aura.anime_updates.features.release.domain.service.ReleaseService;
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
@RequestMapping("/api/anime")
@RequiredArgsConstructor
@Tag(name = "Anime Releases", description = "APIs for browsing and searching anime releases")
public class ReleaseApiController {

    private final ReleaseService releaseService;

    @Operation(summary = "Get all releases", description = "Retrieve paginated list of all anime releases")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Releases retrieved successfully")
    })
    @GetMapping("/get-releases")
    public ResponseEntity<Page<ReleaseInfoResponse>> getReleaseLinks(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0")  Integer page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ){
        Long userId = null;
        if(currentUser != null) {
            userId = currentUser.getId();
        }
        return ResponseEntity.ok(releaseService.getAllReleaseInfo(page, size, userId));
    }

    @Operation(summary = "Search releases", description = "Search anime releases by title or other text")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    })
    @GetMapping("/{searchText}/get-releases")
    public ResponseEntity<Page<ReleaseInfoResponse>> searchAndGetReleaseLinks(
            @Parameter(description = "Search text for anime title", required = true)
            @PathVariable String searchText,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Long userId = null;
        if(currentUser != null) {
            userId = currentUser.getId();
        }
        return ResponseEntity.ok(releaseService.searchAllReleaseInfo(page, size, searchText, userId));
    }

    @Operation(summary = "Get release by ID", description = "Retrieve specific anime release information by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Release information retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Release not found")
    })
    @GetMapping("/get-release/{id}")
    public ResponseEntity<ReleaseInfoResponse> getReleaseInfoById(
            @Parameter(description = "ID of the release", required = true)
            @PathVariable("id") Long id
    ){
        return ResponseEntity.ok(releaseService.getReleaseInfoById(id));
    }

}
