package com.aura.anime_updates.services;

import com.aura.anime_updates.domain.AnimeShow;
import com.aura.anime_updates.dto.AnimeDownloadInfo;
import com.aura.anime_updates.repository.AnimeShowRepository;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.stereotype.Service;


import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GetAnimeLinkService {
    private static final String RSS_URL = "https://subsplease.org/rss/?t&r=1080";

    private final AnimeShowRepository animeShowRepository;
    private final AnimePersistenceService animePersistenceService;

    public GetAnimeLinkService(final AnimeShowRepository animeShowRepository,
                               AnimePersistenceService animePersistenceService){
        this.animeShowRepository = animeShowRepository;
        this.animePersistenceService = animePersistenceService;
    }

    public void fetchAndSaveNewAnimeShows() {
        try {
            URL feedSource = new URL(RSS_URL);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedSource));

            List<AnimeShow> animeShowToSave = new ArrayList<>();
            for (SyndEntry entry : feed.getEntries()) {
                String title = entry.getTitle();
                String link = entry.getLink();

                // Only add if new downloadLink not already in DB
                if (!animeShowRepository.existsByDownloadLink(link)) {
                    AnimeShow anime = new AnimeShow(title, link);
                    animeShowToSave.add(anime);
                }
            }
            if (!animeShowToSave.isEmpty()) {
                animePersistenceService.saveAll(animeShowToSave);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Called by API to fetch all saved anime from DB as DTO list
    public List<AnimeDownloadInfo> getAllAnimeDownloadInfo() {
        return animeShowRepository.findAll()
                .stream()
                .map(a -> new AnimeDownloadInfo(a.getTitle(), a.getDownloadLink()))
                .collect(Collectors.toList());
    }

}

