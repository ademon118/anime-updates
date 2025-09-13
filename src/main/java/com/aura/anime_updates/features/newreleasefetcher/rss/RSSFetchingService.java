package com.aura.anime_updates.features.newreleasefetcher.rss;

import com.aura.anime_updates.features.newreleasefetcher.dto.RSSEntry;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RSSFetchingService {

    private static final String RSS_URL = "https://subsplease.org/rss/?t&r=1080";


    public List<RSSEntry> fetchAnimeRSSFeed() {
        try {
            URL feedSource = new URL(RSS_URL);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedSource));

            List<RSSEntry> rssEntries = new ArrayList<>();

            for(SyndEntry entry : feed.getEntries()) {
                rssEntries.add(
                        RSSEntry.builder()
                                .title(entry.getTitle())
                                .link(entry.getLink())
                                .category(entry.getCategories().get(0).getName())
                                .publishedDate(entry.getPublishedDate())
                                .build()
                );
            }
            return rssEntries;

        } catch(Exception e) {
            log.error("Failed to Fetch RSS Entries with error : {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
