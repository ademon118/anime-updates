package com.aura.anime_updates.features.release.api.response;

import java.time.LocalDateTime;

public interface ReleaseInfoResponse {
    Long getReleaseId();
    Long getAnimeShowId();
    String getShowTitle();
    String getReleaseDownloadLink();
    String getEpisode();
    String getFileName();
    String getImgUrl();
    LocalDateTime getReleasedDate();
}
