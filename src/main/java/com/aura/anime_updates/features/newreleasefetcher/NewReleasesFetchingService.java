package com.aura.anime_updates.features.newreleasefetcher;

import com.aura.anime_updates.features.animeShow.domain.entity.AnimeShow;
import com.aura.anime_updates.features.animeShow.domain.repository.AnimeShowRepository;
import com.aura.anime_updates.features.newreleasefetcher.dataprocessor.DataCleaningService;
import com.aura.anime_updates.features.newreleasefetcher.dataprocessor.DataValidationService;
import com.aura.anime_updates.features.newreleasefetcher.dto.RSSEntry;
import com.aura.anime_updates.features.newreleasefetcher.dto.TransformedEntry;
import com.aura.anime_updates.features.newreleasefetcher.events.NewReleaseEvent;
import com.aura.anime_updates.features.newreleasefetcher.rss.RSSFetchingService;
import com.aura.anime_updates.features.release.domain.entity.Release;
import com.aura.anime_updates.features.release.domain.repository.ReleaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewReleasesFetchingService {

    private final RSSFetchingService rss;
    private final DataValidationService validator;
    private final DataCleaningService cleaner;
    private final AnimeShowRepository animeShowRepository;
    private final ReleaseRepository releaseRepository;
    private final ApplicationEventPublisher publisher;

    public void fetchAndSave() {
        List<RSSEntry> rssEntries = rss.fetchAnimeRSSFeed();
        rssEntries = validator.validateAndFilterRSSEntries(rssEntries);
        List<TransformedEntry> transformedEntries = cleaner.cleanAndTransformEntries(rssEntries);
        transformedEntries.forEach(this::persist);
    }

    @Transactional
    private void persist(TransformedEntry entry) {
        try {
            AnimeShow animeShow = animeShowRepository.findByTitle(entry.animeShowName())
                    .orElseGet(() -> {
                        return animeShowRepository.save(
                                new AnimeShow(
                                        entry.animeShowName(),
                                        entry.imageUrl()
                                )
                        );
                    });

            Release newRelease = releaseRepository.findByEpisodeAndAnimeShow(entry.episode(), animeShow).orElseGet(() -> {
                return new Release(
                            entry.downloadLink(),
                            entry.episode(),
                            entry.releaseVersion(),
                            entry.releasedDate(),
                            entry.fileName(),
                            entry.fileSize(),
                            animeShow
                    );
            });
            newRelease.setDownloadLink(entry.downloadLink());
            newRelease.setReleaseVersion(entry.releaseVersion());
            newRelease.setFileName(entry.fileName());
            newRelease.setFileSize(entry.fileSize());

            releaseRepository.saveAndFlush(newRelease);

            if (!newRelease.isNewVersionRelease()) {
                publisher.publishEvent(new NewReleaseEvent(this, newRelease.getId(), newRelease.getEpisode(), animeShow.getId(), animeShow.getImageUrl()));
            }

        } catch (Exception e) {
            log.error("Failed to save a release with error : {}", e.getMessage());
        }
    }
}
