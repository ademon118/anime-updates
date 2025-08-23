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
    public ReleaseInfoResponse getReleaseInfoById(Long id) {
        log.info("Fetching release info for id={}", id);

        return releaseRepository.findReleaseById(id)
                .map(release -> {
                    log.debug("Found release: {}", release.showTitle());
                    return release;
                })
                .orElseThrow(() -> {
                    log.error("Release not found with id={}", id);
                    return new RuntimeException("Could not find release with id: " + id);
                });
    }
}
