package com.aura.anime_updates.features.animeShow.api;

import com.aura.anime_updates.features.animeShow.domain.entity.AnimeShow;
import com.aura.anime_updates.features.animeShow.domain.service.AnimeShowService;
import com.aura.anime_updates.features.release.api.response.ReleaseInfoResponse;
import com.aura.anime_updates.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anime-show")
@RequiredArgsConstructor
public class AnimeShowApiController {

    private final AnimeShowService animeShowService;

    @GetMapping("/{animeShowId}/get-releases")
    public ResponseEntity<Page<ReleaseInfoResponse>> getReleaseLinks(
            @PathVariable Long animeShowId,
            @RequestParam(defaultValue = "0")  Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ){
        Long userId = null;
        if(currentUser != null) {
            userId = currentUser.getId();
        }
        return ResponseEntity.ok(animeShowService.getAllReleaseInfoOfAnAnimeShow(page, size, animeShowId, userId));
    }
}
