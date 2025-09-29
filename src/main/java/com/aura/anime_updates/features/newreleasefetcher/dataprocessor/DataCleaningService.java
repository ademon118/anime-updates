package com.aura.anime_updates.features.newreleasefetcher.dataprocessor;

import com.aura.anime_updates.features.newreleasefetcher.dataprocessor.utilities.DataProcessingUtils;
import com.aura.anime_updates.features.newreleasefetcher.dto.RSSEntry;
import com.aura.anime_updates.features.newreleasefetcher.dto.TransformedEntry;
import com.aura.anime_updates.features.newreleasefetcher.imagefetcher.ImageFetchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCleaningService {

    private final ImageFetchingService imageFetcher;

    public List<TransformedEntry> cleanAndTransformEntries(List<RSSEntry> entries) {
        List<TransformedEntry> transformedEntries = new ArrayList<>();
        entries.forEach(entry -> {
            transformedEntries.add(
                    TransformedEntry.builder()
                            .animeShowName(DataProcessingUtils.getAnimeShowTitleFromCategory(entry.category()))
                            .episode(DataProcessingUtils.getEpisodeFromRawTitle(entry.title()))
                            .releaseVersion(DataProcessingUtils.getReleaseVersionFromRawTitle(entry.title()))
                            .downloadLink(entry.link())
                            .fileSize(entry.size())
                            .fileName(entry.title())
                            .releasedDate(transformPublishedDate(entry.publishedDate()))
                            .imageUrl(imageFetcher.fetchImageForAnimeShow(DataProcessingUtils.getAnimeShowTitleFromCategory(entry.category())))
                            .build()
            );

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.error("Error in thread sleep");
            }

        });
        return transformedEntries;
    }

    private LocalDateTime transformPublishedDate(Date releasedDate) {
        return releasedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
