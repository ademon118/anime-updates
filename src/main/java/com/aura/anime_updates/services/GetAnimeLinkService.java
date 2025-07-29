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

@Service
public class GetAnimeLinkService {
    private static final String RSS_URL = "https://subsplease.org/rss/?t&r=1080";

    private final AnimeShowRepository animeShowRepository;

    public GetAnimeLinkService(final AnimeShowRepository animeShowRepository){
        this.animeShowRepository = animeShowRepository;
    }

    public List<AnimeDownloadInfo> fetchAnimeDownloadLinks(){
        List<AnimeDownloadInfo> downloads = new ArrayList<>();
        try {
            URL feedSource = new URL(RSS_URL);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedSource));

            for (SyndEntry entry : feed.getEntries()) {
                String title = entry.getTitle();
                String link = entry.getLink();
                AnimeShow anime = new AnimeShow(title,link);
                animeShowRepository.save(anime);
                downloads.add(new AnimeDownloadInfo(title, link));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

       return  downloads;
    }
}

