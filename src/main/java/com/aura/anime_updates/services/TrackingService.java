package com.aura.anime_updates.services;

import com.aura.anime_updates.domain.AnimeShow;
import com.aura.anime_updates.domain.Release;
import com.aura.anime_updates.domain.User;
import com.aura.anime_updates.repository.AnimeShowRepository;
import com.aura.anime_updates.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TrackingService {

    private final UserRepository userRepository;
    private final AnimeShowRepository animeShowRepository;

    public TrackingService(UserRepository userRepository, AnimeShowRepository animeShowRepository) {
        this.userRepository = userRepository;
        this.animeShowRepository = animeShowRepository;
    }

    @Transactional
    public void trackShow(long userId, long showId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        AnimeShow show = animeShowRepository.findById(showId).orElseThrow(() -> new IllegalArgumentException("Anime show not found"));
        user.trackShow(show);
        userRepository.save(user);
    }

    @Transactional
    public void untrackShow(long userId, long showId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        AnimeShow show = animeShowRepository.findById(showId).orElseThrow(() -> new IllegalArgumentException("Anime show not found"));
        user.untrackShow(show);
        userRepository.save(user);
    }

    public Set<AnimeShow> getTrackedShows(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getTrackedShows();
    }

    public List<Release> getTrackedReleases(long userId) {
        return getTrackedShows(userId).stream()
                .flatMap(show -> show.getReleases().stream())
                .sorted((a, b) -> b.getReleasedDate().compareTo(a.getReleasedDate()))
                .collect(Collectors.toList());
    }
}


