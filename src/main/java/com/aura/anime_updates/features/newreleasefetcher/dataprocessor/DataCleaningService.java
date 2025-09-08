package com.aura.anime_updates.features.newreleasefetcher.dataprocessor;

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
    Pattern episodePatternRegex = Pattern.compile(".*-\\s*([^\\(\\s]+)\\s*\\(1080p\\)");

    public List<TransformedEntry> cleanAndTransformEntries(List<RSSEntry> entries) {
        List<TransformedEntry> transformedEntries = new ArrayList<>();
        entries.forEach(entry -> {
            transformedEntries.add(
                    TransformedEntry.builder()
                            .animeShowName(transformAnimeShowTitle(entry.category()))
                            .episode(transformEpisode(entry.title()))
                            .downloadLink(entry.link())
                            .fileName(entry.title())
                            .releasedDate(transformPublishedDate(entry.publishedDate()))
                            .imageUrl(imageFetcher.fetchImageForAnimeShow(transformAnimeShowTitle(entry.category())))
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

    private String transformEpisode (String rawTitle) {
        Matcher matcher = episodePatternRegex.matcher(rawTitle);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String transformAnimeShowTitle(String category) {
        return category.replaceAll("\\s*-\\s*1080", "").trim();
    }

    private LocalDateTime transformPublishedDate(Date releasedDate) {
        return releasedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
