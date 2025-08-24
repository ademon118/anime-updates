package com.aura.anime_updates.features.release.domain.service.impl;

import com.aura.anime_updates.features.release.api.response.ReleaseInfoResponse;
import com.aura.anime_updates.features.release.domain.dto.ReleaseInfoDTO;
import com.aura.anime_updates.features.release.domain.mapper.ReleaseMapper;
import com.aura.anime_updates.features.release.domain.repository.ReleaseRepository;
import com.aura.anime_updates.features.release.domain.service.ReleaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReleaseServiceImpl implements ReleaseService {

    private final ReleaseRepository releaseRepository;
    private final ReleaseMapper releaseMapper;

    @Override
    public Page<ReleaseInfoResponse> getAllReleaseInfo(Integer page, Integer size, @Nullable Long userId){
        log.info("Fetching releases with page={} and size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);

        try{
            Page<ReleaseInfoDTO> releaseInfo =  releaseRepository.getReleaseInfo(pageable, userId);
            log.debug("Fetched {} releases out of total {}",
                    releaseInfo.getNumberOfElements(), releaseInfo.getTotalElements());

            return releaseMapper.toResponsePage(releaseInfo);
        } catch (DataAccessException e){
            throw new RuntimeException("Database error while fetching releases", e);
        }
    }

    @Override
    public Page<ReleaseInfoResponse> searchAllReleaseInfo(Integer page, Integer size, String searchText, @Nullable Long userId){
        log.info("Fetching releases with page={} and size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);

        try{
            Page<ReleaseInfoDTO> releaseInfo =  releaseRepository.getReleaseInfoWithSearchText(pageable, searchText.toLowerCase(), userId);
            log.debug("Fetched {} releases out of total {}",
                    releaseInfo.getNumberOfElements(), releaseInfo.getTotalElements());

            return releaseMapper.toResponsePage(releaseInfo);
        } catch (DataAccessException e){
            throw new RuntimeException("Database error while fetching releases", e);
        }
    }

    @Override
    public ReleaseInfoResponse getReleaseInfoById(Long id) {
        log.info("Fetching release info for id={}", id);

        return releaseMapper.toResponse(releaseRepository.findReleaseById(id).get());
    }

    @Override
    public Page<ReleaseInfoResponse> getReleaseInfoByAnimeShow(Integer page, Integer size, Long animeShowId, Long userId) {
        log.info("Fetching release infos for anime show id={}", animeShowId);

        Pageable pageable = PageRequest.of(page, size);
        try {
            Page<ReleaseInfoDTO> releases = releaseRepository.getAllReleasesOfAnimeShow(pageable, animeShowId, userId);

            return releaseMapper.toResponsePage(releases);
        } catch (Exception e) {
            throw new RuntimeException("Database error while fetching releases", e);
        }
    }

    @Override
    public Page<ReleaseInfoResponse> getAllTrackedReleaseInfo(Integer page, Integer size, Long userId) {
        log.info("Fetching tracked releases by userId={} with page={} and size={}", userId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        System.out.println(userId);
        try{
            Page<ReleaseInfoDTO> trackedReleaseInfo =  releaseRepository.getAllTrackedReleases(pageable, userId);
            log.debug("Fetched {} releases out of total {}",
                    trackedReleaseInfo.getNumberOfElements(), trackedReleaseInfo.getTotalElements());

            return this.releaseMapper.toResponsePage(trackedReleaseInfo);
        } catch (DataAccessException e){
            throw new RuntimeException("Database error while fetching releases", e);
        }
    }
}