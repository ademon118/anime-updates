package com.aura.anime_updates.features.newreleasefetcher.dataprocessor;

import com.aura.anime_updates.features.newreleasefetcher.dataprocessor.utilities.DataProcessingUtils;
import com.aura.anime_updates.features.newreleasefetcher.dto.RSSEntry;
import com.aura.anime_updates.features.release.domain.repository.ReleaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataValidationService {

    private final ReleaseRepository releaseRepository;

    private boolean validateRSSEntryIntegrity(RSSEntry entry) {
        return entry.title() != null &&
                entry.link() != null &&
                entry.category() != null &&
                entry.publishedDate() != null;
    }

    private boolean validateNonNewEntry(RSSEntry entry) {
        return !releaseRepository.existsByFileName(entry.title());
    }

    private boolean validateDeprecatedEntry(RSSEntry entry) {
        return releaseRepository.newerVersionExists(
                DataProcessingUtils.getAnimeShowTitleFromCategory(entry.category()),
                DataProcessingUtils.getEpisodeFromRawTitle(entry.title()),
                DataProcessingUtils.getReleaseVersionFromRawTitle(entry.title())
        ).compareTo(0L) == 0;
    }

    private boolean validateNonBatchReleases(RSSEntry entry) {
        return !DataProcessingUtils.isBatchRelease(entry.title());
    }

    private List<RSSEntry> filterInvalidEntries(List<RSSEntry> entries) {
        return entries.stream()
                .filter(this::validateRSSEntryIntegrity)
                .filter(this::validateNonNewEntry)
                .filter(this::validateDeprecatedEntry)
                .filter(this::validateNonBatchReleases)
                .toList();
    }

    public List<RSSEntry> validateAndFilterRSSEntries(List<RSSEntry> entries) {
        return filterInvalidEntries(entries);
    }
}