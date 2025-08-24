package com.aura.anime_updates.features.release.api;

import com.aura.anime_updates.features.release.api.response.ReleaseInfoResponse;
import com.aura.anime_updates.features.release.domain.service.ReleaseService;
import com.aura.anime_updates.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anime")
@RequiredArgsConstructor
public class ReleaseApiController {

    private final ReleaseService releaseService;

    @GetMapping("/get-releases")
    public ResponseEntity<Page<ReleaseInfoResponse>> getReleaseLinks(
            @RequestParam(defaultValue = "0")  Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ){
        Long userId = null;
        if(currentUser != null) {
            userId = currentUser.getId();
        }
        return ResponseEntity.ok(releaseService.getAllReleaseInfo(page, size, userId));
    }

    @GetMapping("/{searchText}/get-releases")
    public ResponseEntity<Page<ReleaseInfoResponse>> searchAndGetReleaseLinks(
            @PathVariable String searchText,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Long userId = null;
        if(currentUser != null) {
            userId = currentUser.getId();
        }
        return ResponseEntity.ok(releaseService.searchAllReleaseInfo(page, size, searchText, userId));
    }

    @GetMapping("/get-release/{id}")
    public ResponseEntity<ReleaseInfoResponse> getReleaseInfoById(
            @PathVariable("id") Long id
    ){
        return ResponseEntity.ok(releaseService.getReleaseInfoById(id));
    }

}
