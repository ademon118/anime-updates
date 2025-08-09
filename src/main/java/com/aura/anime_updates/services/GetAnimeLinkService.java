package com.aura.anime_updates.services;

import com.aura.anime_updates.domain.AnimeShow;
import com.aura.anime_updates.dto.AnimeDownloadInfo;
import com.aura.anime_updates.dto.AnimeDownloadInfoPage;
import com.aura.anime_updates.repository.AnimeShowRepository;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;


import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
                String rawTitle = entry.getTitle();
                String link = entry.getLink();
                String category = entry.getCategories().isEmpty() ? null : entry.getCategories().get(0).getName();
                LocalDateTime releasedDate = entry.getPublishedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                //To get only episode number from title
                String episode = extractEpisodeNumber(rawTitle);
                String filename = rawTitle.replaceAll("\\.mkv$", "");


                if( episode == null ||  category == null || !link.contains("nyaa.si")){
                    continue;
                }

                //Title from category removing -1080
                String cleanTitle = category.replaceAll("\\s*-\\s*1080", "").trim();


                // Only add if new downloadLink not already in DB
                if (!animeShowRepository.existsByDownloadLink(link)) {
                    AnimeShow anime = new AnimeShow(cleanTitle,link,episode,releasedDate,filename);
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
        return animeShowRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(a -> new AnimeDownloadInfo(a.getId(), a.getTitle(), a.getDownloadLink(),a.getEpisode(),a.getCreatedAt(),a.getFileName()))
                .collect(Collectors.toList());
    }


    //Method for title trim
    private String extractEpisodeNumber(String title){
        Pattern pattern = Pattern.compile("\\-\\s*(\\d{2})\\s*\\(");
        Matcher matcher = pattern.matcher(title);
        return matcher.find()?matcher.group(1) : null;
    }

    public AnimeDownloadInfoPage getAllAnimeDownloadInfoPaginated(int page,int size){
        PageRequest pageable = PageRequest.of(page,size);
        Page<AnimeShow> animePage = animeShowRepository.findAllByOrderByCreatedAtDesc(pageable);
        return toDtoPage(animePage);
    }

    public AnimeDownloadInfoPage searchByTitle(String title,int page, int size){
        PageRequest pageable = PageRequest.of(page,size);
        Page<AnimeShow> animePage = animeShowRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(title,pageable);
        return toDtoPage(animePage);
    }

    private AnimeDownloadInfoPage toDtoPage(Page<AnimeShow> animePage){
        List<AnimeDownloadInfo> content = animePage.getContent().stream()
                .map(a -> new AnimeDownloadInfo(
                        a.getId(),
                        a.getTitle(),
                        a.getDownloadLink(),
                        a.getEpisode(),
                        a.getCreatedAt(),
                        a.getFileName()
                )).collect(Collectors.toList());

        return new AnimeDownloadInfoPage(content, animePage.getTotalElements(),animePage.getTotalPages());
    }

}

