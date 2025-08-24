package com.aura.anime_updates.features.animeShow.domain.service;

import com.aura.anime_updates.features.release.api.response.ReleaseInfoResponse;
import com.aura.anime_updates.features.release.domain.service.ReleaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AnimeShowService {

    private final ReleaseService releaseService;

    public Page<ReleaseInfoResponse> getAllReleaseInfoOfAnAnimeShow(Integer page, Integer size,
                                                                    Long animeShowId, Long userId) {

        return releaseService.getReleaseInfoByAnimeShow(page, size, animeShowId, userId);

    }
}
