package com.aura.anime_updates.features.newreleasefetcher.dto;

import lombok.Builder;

import java.util.Date;

@Builder
public record RSSEntry (
        String title,
        String link,
        String size,
        String category,
        Date publishedDate
) {}
