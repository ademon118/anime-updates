package com.aura.anime_updates.api.response;

public record ReleaseInfoResponse(
    Long releaseId,
    Long animeShowId,
    String showTitle,
    String releaseDownloadLink,
    String episode,
    String fileName,
    String imgUrl,
    Boolean tracked
) {
}
