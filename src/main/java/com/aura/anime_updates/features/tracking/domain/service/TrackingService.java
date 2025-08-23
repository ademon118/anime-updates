package com.aura.anime_updates.features.tracking.domain.service;

import com.aura.anime_updates.features.animeShow.domain.entity.AnimeShow;
import com.aura.anime_updates.features.animeShow.domain.repository.AnimeShowRepository;
import com.aura.anime_updates.features.user.domain.entity.User;
import com.aura.anime_updates.features.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final AnimeShowRepository animeShowRepository;
    private final UserRepository userRepository;

    public void trackAnimeShow(Long userId, Long animeShowId) {
        AnimeShow animeShowToTrack = animeShowRepository.findById(animeShowId)
                .orElseThrow();

        User user = userRepository.findById(userId)
                        .orElseThrow();

        animeShowToTrack.addTracker(user);

        animeShowRepository.saveAndFlush(animeShowToTrack);
    }

    public void unTrackAnimeShow(Long userId, Long animeShowId) {
        AnimeShow animeShowToUntrack = animeShowRepository.findById(animeShowId)
                .orElseThrow();

        User user = userRepository.findById(userId)
                .orElseThrow();

        animeShowToUntrack.removeTracker(user);
        animeShowRepository.saveAndFlush(animeShowToUntrack);
    }
}
