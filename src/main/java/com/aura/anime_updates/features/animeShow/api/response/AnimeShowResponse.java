package com.aura.anime_updates.features.animeShow.api.response;

import lombok.Builder;

@Builder
public record AnimeShowResponse(
    Long id,
    String title,
    String imageUrl
) { }