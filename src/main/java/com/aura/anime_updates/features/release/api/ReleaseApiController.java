package com.aura.anime_updates.features.release.api;

import com.aura.anime_updates.features.release.api.response.ReleaseInfoResponse;
import com.aura.anime_updates.features.release.domain.service.ReleaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anime")
@RequiredArgsConstructor
public class ReleaseApiController {

    private final ReleaseService releaseService;

    @GetMapping("/get-releases")
    public ResponseEntity<Page<ReleaseInfoResponse>> getReleaseLinks(
            @RequestParam(defaultValue = "0")  Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ){
        Page<ReleaseInfoResponse> releasePage = releaseService.getAllReleaseInfo(page, size);
        return ResponseEntity.ok(releasePage);
    }

    @GetMapping("/get-release/{id}")
    public ResponseEntity<ReleaseInfoResponse> getReleaseInfoById(
            @PathVariable("id") Long id
    ){
        return ResponseEntity.ok(releaseService.getReleaseInfoById(id));
    }
}
