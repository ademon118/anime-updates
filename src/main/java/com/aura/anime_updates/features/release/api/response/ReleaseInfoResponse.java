package com.aura.anime_updates.features.release.api.response;

import java.time.LocalDateTime;

public record ReleaseInfoResponse(
        Long releaseId,
        Long animeShowId,
        String showTitle,
        String downloadLink,
        String episode,
        String fileName,
        String imgUrl,
        LocalDateTime releasedDate,
        Boolean tracked
) {}