package com.aura.anime_updates.services;

import com.aura.anime_updates.domain.AnimeShow;
import com.aura.anime_updates.repository.AnimeShowRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnimePersistenceService {

    private final AnimeShowRepository animeShowRepository;

    public AnimePersistenceService(AnimeShowRepository animeShowRepository){
        this.animeShowRepository = animeShowRepository;
    }

    public void saveAll(List<AnimeShow> shows){
        animeShowRepository.saveAll(shows);
    }

}
