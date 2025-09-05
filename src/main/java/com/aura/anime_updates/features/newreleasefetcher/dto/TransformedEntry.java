package com.aura.anime_updates.features.newreleasefetcher.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TransformedEntry (
        String animeShowName,
        String episode,
        String downloadLink,
        LocalDateTime releasedDate,
        String fileName,
        String imageUrl
) {}