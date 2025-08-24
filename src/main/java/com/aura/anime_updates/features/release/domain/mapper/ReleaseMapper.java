package com.aura.anime_updates.features.release.domain.mapper;

import com.aura.anime_updates.features.release.api.response.ReleaseInfoResponse;
import com.aura.anime_updates.features.release.domain.dto.ReleaseInfoDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

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

    public Map<String, String> toMap(ReleaseInfoResponse release) {
        Map<String, String> map = new LinkedHashMap<>();

        map.put("releaseId", String.valueOf(release.releaseId()));
        map.put("animeShowId", String.valueOf(release.animeShowId()));
        map.put("showTitle", release.showTitle());
        map.put("releaseDownloadLink", release.releaseDownloadLink());
        map.put("episode", release.episode());
        map.put("fileName", release.fileName());
        map.put("imgUrl", release.imgUrl());
        map.put("releasedDate", release.releasedDate().toString());
        map.put("tracked", String.valueOf(release.tracked()));

        return map;
    }
}
