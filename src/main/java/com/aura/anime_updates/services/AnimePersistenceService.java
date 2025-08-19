package com.aura.anime_updates.services;

import com.aura.anime_updates.domain.AnimeShow;
import com.aura.anime_updates.domain.Release;
import com.aura.anime_updates.repository.AnimeShowRepository;
import com.aura.anime_updates.repository.ReleaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnimePersistenceService {

    private final AnimeShowRepository animeShowRepository;
    private final ReleaseRepository releaseRepository;

    public AnimePersistenceService(AnimeShowRepository animeShowRepository, ReleaseRepository releaseRepository){
        this.animeShowRepository = animeShowRepository;
        this.releaseRepository = releaseRepository;
    }

    public void saveAllShows(List<AnimeShow> shows){
        animeShowRepository.saveAll(shows);
    }

    public void saveAllReleases(List<Release> releases){
        releaseRepository.saveAll(releases);
    }

}
