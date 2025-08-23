package com.aura.anime_updates.features.release.domain.dto;

import java.time.LocalDateTime;

public interface ReleaseInfoDTO {
    Long getReleaseId();
    Long getAnimeShowId();
    String getShowTitle();
    String getReleaseDownloadLink();
    String getEpisode();
    String getFileName();
    String getImgUrl();
    LocalDateTime getReleasedDate();
    Integer getTracked(); // raw 0/1 from MySQL
}