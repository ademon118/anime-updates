package com.aura.anime_updates.features.animeShow.domain.service;

import com.aura.anime_updates.features.animeShow.api.response.AnimeShowResponse;
import com.aura.anime_updates.features.animeShow.domain.repository.AnimeShowRepository;
import com.aura.anime_updates.features.release.api.response.ReleaseInfoResponse;
import com.aura.anime_updates.features.release.domain.dto.ReleaseInfoDTO;
import com.aura.anime_updates.features.release.domain.service.ReleaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class AnimeShowService {

    private final ReleaseService releaseService;
    private final AnimeShowRepository animeShowRepository;

    public Page<ReleaseInfoResponse> getAllReleaseInfoOfAnAnimeShow(Integer page, Integer size,
                                                                    Long animeShowId, Long userId) {

        return releaseService.getReleaseInfoByAnimeShow(page, size, animeShowId, userId);

    }

    public Page<AnimeShowResponse> searchAnimeShows(Integer page, Integer size, String searchText) {
        Pageable pageable = PageRequest.of(page, size);
        try {
            return animeShowRepository.getAnimeShowWithSearchText(pageable, searchText.toLowerCase());
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error while fetching anime shows", e);
        }
    }

    public Page<AnimeShowResponse> getAllTrackedAnimeShows(Integer page, Integer size,
                                                           Long userId) {
        log.info("Fetching tracked anime shows by userId={} with page={} and size={}", userId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        try{
            Page<AnimeShowResponse> trackedAnimeShows = animeShowRepository.getAllTrackedShowsByUser(pageable, userId);
            log.debug("Fetched {} anime shows out of total {}",
                    trackedAnimeShows.getNumberOfElements(), trackedAnimeShows.getTotalElements());

            return trackedAnimeShows;
        } catch (DataAccessException e){
            throw new RuntimeException("Database error while fetching anime shows", e);
        }
    }
}
