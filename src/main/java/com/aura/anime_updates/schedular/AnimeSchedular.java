package com.aura.anime_updates.schedular;


import com.aura.anime_updates.services.GetAnimeLinkService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AnimeSchedular {
    private final GetAnimeLinkService getAnimeLinkService;

    public AnimeSchedular(GetAnimeLinkService getAnimeLinkService){
        this.getAnimeLinkService = getAnimeLinkService;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void fetchAnimeEveryFiveMinute(){
        getAnimeLinkService.fetchAndSaveNewAnimeShows();
    }

    // Backfill missing images every 30 minutes
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void backfillMissingImagesEveryThirtyMinutes(){
        getAnimeLinkService.backfillMissingImages();
    }

}
