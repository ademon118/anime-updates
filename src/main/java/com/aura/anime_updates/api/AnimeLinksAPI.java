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
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId
    ) {
        try {
            AnimeDownloadInfoPage result;
            if (title != null && !title.isBlank()) {
                result = getAnimeLinkService.searchByTitle(title, page, size, userId);
            } else {
                result = getAnimeLinkService.getAllAnimeDownloadInfoPaginated(page, size, userId);
            }
            return ResponseEntity.ok(ApiResponse.success(result, "Anime downloads retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve anime downloads: " + e.getMessage(), 400));
        }
    }

    @GetMapping("/downloads/all")
    public ResponseEntity<ApiResponse<List<AnimeDownloadInfo>>> getAllDownloadLinks(
            @RequestParam(required = false) Long userId
    ) {
        try {
            List<AnimeDownloadInfo> result = getAnimeLinkService.getAllAnimeDownloadInfo(userId);
            return ResponseEntity.ok(ApiResponse.success(result, "All anime downloads retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve all anime downloads: " + e.getMessage(), 400));
        }
    }

}
