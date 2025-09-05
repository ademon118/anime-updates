package com.aura.anime_updates.schedular;


import com.aura.anime_updates.features.newreleasefetcher.NewReleasesFetchingService;
import com.aura.anime_updates.features.newreleasefetcher.imagefetcher.ImageFetchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnimeSchedular {
    private final NewReleasesFetchingService newReleasesFetchingService;
    private final ImageFetchingService imageFetchingService;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void fetchAnimeEveryFiveMinute(){
        newReleasesFetchingService.fetchAndSave();
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void backfillMissingImagesEveryThirtyMinutes(){
        imageFetchingService.backfillMissingImages();
    }

}
