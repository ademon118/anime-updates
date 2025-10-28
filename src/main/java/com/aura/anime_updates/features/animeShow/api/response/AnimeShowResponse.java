package com.aura.anime_updates.features.animeShow.api.response;

import java.time.LocalDateTime;

public interface AnimeShowResponse {
    Long getId();
    String getTitle();
    String getImageUrl();
    LocalDateTime getLatestReleasedTime();
}