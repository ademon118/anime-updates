package com.aura.anime_updates.features.release.domain.mapper;

import com.aura.anime_updates.features.release.api.response.ReleaseInfoResponse;
import com.aura.anime_updates.features.release.domain.dto.ReleaseInfoDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ReleaseMapper {
    public ReleaseInfoResponse toResponse(ReleaseInfoDTO dto) {

        return new ReleaseInfoResponse(
                dto.getReleaseId(),
                dto.getAnimeShowId(),
                dto.getShowTitle(),
                dto.getReleaseDownloadLink(),
                dto.getEpisode(),
                dto.getFileName(),
                dto.getImgUrl(),
                dto.getReleasedDate(),
                dto.getTracked() != null && dto.getTracked() == 1
        );
    }

    public Page<ReleaseInfoResponse> toResponsePage(Page<ReleaseInfoDTO> page) {
        return page.map(this::toResponse);
    }
}
