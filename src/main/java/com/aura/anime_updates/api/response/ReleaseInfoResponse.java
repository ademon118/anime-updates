package com.aura.anime_updates.api.response;

public interface ReleaseInfoResponse {
    Long getReleaseId();
    Long getAnimeShowId();
    String getShowTitle();
    String getReleaseDownloadLink();
    String getEpisode();
    String getFileName();
    String getImgUrl();
}
