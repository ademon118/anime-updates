package com.aura.anime_updates.features.newreleasefetcher.dataprocessor;

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

    private List<RSSEntry> filterInvalidEntries(List<RSSEntry> entries) {
        return entries.stream()
                .filter(this::validateRSSEntryIntegrity)
                .toList();
    }

    private List<RSSEntry> filterNonNewEntries(List<RSSEntry> entries) {
        return entries.stream()
                .filter(entry -> !releaseRepository.existsByFileName(entry.title()))
                .toList();
    }

    public List<RSSEntry> validateAndFilterRSSEntries(List<RSSEntry> entries) {
        return filterNonNewEntries(filterInvalidEntries(entries));
    }
}