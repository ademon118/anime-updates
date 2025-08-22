package com.aura.anime_updates.api;

import com.aura.anime_updates.api.response.ReleaseInfoResponse;
import com.aura.anime_updates.dto.AnimeDownloadInfo;
import com.aura.anime_updates.dto.AnimeDownloadInfoPage;
import com.aura.anime_updates.services.GetAnimeLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/anime")
public class AnimeLinksAPI {
    @Autowired
    private  GetAnimeLinkService getAnimeLinkService;

    @GetMapping("/downloads")
    public ResponseEntity<AnimeDownloadInfoPage> getDownloadLinks(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (title != null && !title.isBlank()) {
            return ResponseEntity.ok(getAnimeLinkService.searchByTitle(title, page, size));
        } else {
            return ResponseEntity.ok(getAnimeLinkService.getAllAnimeDownloadInfoPaginated(page, size));
        }
    }

    @GetMapping("/get-releases")
    public ResponseEntity<Page<ReleaseInfoResponse>> getReleaseLinks(
            @RequestParam(defaultValue = "0")  Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ){
        Page<ReleaseInfoResponse> releasePage = getAnimeLinkService.getAllReleaseInfo(page, size);
        return ResponseEntity.ok(releasePage);
    }

    @GetMapping("/get-release/{id}")
    public ResponseEntity<ReleaseInfoResponse> getReleaseInfoById(
            @PathVariable("id") Long id
    ){
        return ResponseEntity.ok(getAnimeLinkService.getReleaseInfoById(id));
    }
}
