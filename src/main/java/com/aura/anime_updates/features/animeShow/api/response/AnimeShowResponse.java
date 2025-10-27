package com.aura.anime_updates.features.animeShow.api.response;

import java.time.LocalDateTime;

public record AnimeShowResponse(
        Long id,
        String title,
        String imageUrl,
        LocalDateTime latestReleasedTime
) {}