package com.aura.anime_updates.services;

import com.aura.anime_updates.domain.AnimeShow;
import com.aura.anime_updates.features.release.domain.entity.Release;
import com.aura.anime_updates.domain.User;
import com.aura.anime_updates.dto.TrackedReleaseDto;
import com.aura.anime_updates.dto.TrackedShowDto;
import com.aura.anime_updates.repository.AnimeShowRepository;
import com.aura.anime_updates.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrackingService {

    private static final Logger logger = LoggerFactory.getLogger(TrackingService.class);

    private final UserRepository userRepository;
    private final AnimeShowRepository animeShowRepository;

    public TrackingService(UserRepository userRepository, AnimeShowRepository animeShowRepository) {
        this.userRepository = userRepository;
        this.animeShowRepository = animeShowRepository;
    }

    @Transactional
    public void trackShow(long userId, long showId) {
        logger.info("Tracking show {} for user {}", showId, userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        AnimeShow show = animeShowRepository.findById(showId).orElseThrow(() -> new IllegalArgumentException("Anime show not found"));
        user.trackShow(show);
        userRepository.save(user);
        logger.info("Successfully tracked show {} for user {}", showId, userId);
    }

    @Transactional
    public void untrackShow(long userId, long showId) {
        logger.info("Untracking show {} for user {}", showId, userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        AnimeShow show = animeShowRepository.findById(showId).orElseThrow(() -> new IllegalArgumentException("Anime show not found"));
        user.untrackShow(show);
        userRepository.save(user);
        logger.info("Successfully untracked show {} for user {}", showId, userId);
    }

    @Transactional(readOnly = true)
    public Page<TrackedShowDto> getTrackedShows(long userId, Pageable pageable) {
        logger.info("Getting tracked shows for user {} with pagination: page={},size={}", userId,pageable.getPageNumber(),pageable.getPageSize());
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<TrackedShowDto> allShows = user.getTrackedShows().stream()
                        .map(show->new TrackedShowDto(
                                show.getId(),
                                show.getTitle(),
                                show.getImageUrl(),
                                show.getCreatedAt(),
                                show.getUpdatedAt()
                        ))
                        .sorted((a,b)->b.getCreatedAt().compareTo(a.getCreatedAt()))
                        .collect(Collectors.toList());

                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()),allShows.size());
                List<TrackedShowDto> pageContent = allShows.subList(start,end);
                Page<TrackedShowDto> page = new PageImpl<>(pageContent,pageable,allShows.size());

        logger.info("Found {} tracked shows for user {} (showing {} on page {})",
                allShows.size(), userId, pageContent.size(), pageable.getPageNumber());
        return page;
    }

    @Transactional(readOnly = true)
    public Page<TrackedReleaseDto> getTrackedReleases(long userId,Pageable pageable) {
        logger.info("Getting tracked releases for user {} with pagination page={},size={}", userId,pageable.getPageNumber(),pageable.getPageSize());
        User user = userRepository.findById(userId).orElseThrow(()->new IllegalArgumentException("User not found"));

        //WHAT THE FUCK IS THIS SHIT?
        //TODO: FIX ASAP. MY SERVER ONLY HAS 1GB RUN
        List<TrackedReleaseDto> allReleases = user.getTrackedShows().stream()
                .flatMap(show->show.getReleases().stream())
                .map(Release::toTrackedReleaseDto)
                .sorted((a,b)->b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start+pageable.getPageSize()),allReleases.size());

        List<TrackedReleaseDto> pageContent = allReleases.subList(start,end);
        Page<TrackedReleaseDto> page = new PageImpl<>(pageContent,pageable,allReleases.size());


        logger.info("Found {} tracked releases for user {} (showing {} on page {})",
                allReleases.size(), userId, pageContent.size(), pageable.getPageNumber());
        return page;
    }
}


