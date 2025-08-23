package com.aura.anime_updates.features.release.domain.service;

import com.aura.anime_updates.features.release.api.response.ReleaseInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.lang.Nullable;

public interface ReleaseService {
    Page<ReleaseInfoResponse> getAllReleaseInfo(Integer page, Integer size, @Nullable Long userId);

    ReleaseInfoResponse getReleaseInfoById(Long id);
}
