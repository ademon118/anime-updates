package com.aura.anime_updates.api;

import com.aura.anime_updates.dto.AnimeDownloadInfo;
import com.aura.anime_updates.dto.AnimeDownloadInfoPage;
import com.aura.anime_updates.dto.ApiResponse;
import com.aura.anime_updates.services.GetAnimeLinkService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<ApiResponse<AnimeDownloadInfoPage>> getDownloadLinks(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            AnimeDownloadInfoPage result;
            if (title != null && !title.isBlank()) {
                result = getAnimeLinkService.searchByTitle(title, page, size);
            } else {
                result = getAnimeLinkService.getAllAnimeDownloadInfoPaginated(page, size);
            }
            return ResponseEntity.ok(ApiResponse.success(result, "Anime downloads retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve anime downloads: " + e.getMessage(), 400));
        }
    }

    @GetMapping("/downloads/all")
    public ResponseEntity<ApiResponse<List<AnimeDownloadInfo>>> getAllDownloadLinks() {
        try {
            List<AnimeDownloadInfo> result = getAnimeLinkService.getAllAnimeDownloadInfo();
            return ResponseEntity.ok(ApiResponse.success(result, "All anime downloads retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve all anime downloads: " + e.getMessage(), 400));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refreshNow() {
        try {
            getAnimeLinkService.fetchAndSaveNewAnimeShows();
            return ResponseEntity.ok(ApiResponse.success("Fetch and save triggered"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to refresh: " + e.getMessage(), 400));
        }
    }
}
