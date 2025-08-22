package com.aura.anime_updates.features.release.domain.service;

import com.aura.anime_updates.features.release.api.response.ReleaseInfoResponse;
import org.springframework.data.domain.Page;

public interface ReleaseService {
    Page<ReleaseInfoResponse> getAllReleaseInfo(Integer page, Integer size);
    ReleaseInfoResponse getReleaseInfoById(Long id);
}
