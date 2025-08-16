package com.aura.anime_updates.services;

import com.aura.anime_updates.domain.AnimeShow;
import com.aura.anime_updates.dto.AnimeDownloadInfo;
import com.aura.anime_updates.dto.AnimeDownloadInfoPage;
import com.aura.anime_updates.repository.AnimeShowRepository;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private static final String JIKAN_API_BASE = "https://api.jikan.moe/v4/anime?q=";

    private final AnimeShowRepository animeShowRepository;
    private final AnimePersistenceService animePersistenceService;

    public GetAnimeLinkService(final AnimeShowRepository animeShowRepository,
                               AnimePersistenceService animePersistenceService){
        this.animeShowRepository = animeShowRepository;
        this.animePersistenceService = animePersistenceService;
    }


    //Fetches RSS data, queries Jikan API for images, and stores new anime
    public void fetchAndSaveNewAnimeShows() {
        try {
            URL feedSource = new URL(RSS_URL);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedSource));

            List<AnimeShow> animeShowToSave = new ArrayList<>();
            RestTemplate restTemplate = new RestTemplate();

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
                    String imageUrl = fetchImageFromJikan(cleanTitle,restTemplate);

                    AnimeShow anime = new AnimeShow(cleanTitle,link,episode,releasedDate,filename,imageUrl);
                    anime.setImageUrl(imageUrl);
                    animeShowToSave.add(anime);

                    Thread.sleep(5000);
                }
            }
            if (!animeShowToSave.isEmpty()) {
                animePersistenceService.saveAll(animeShowToSave);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Backfills missing images for existing records.
    public void backfillMissingImages(){
        try {
            List<AnimeShow> showsWithoutImages = animeShowRepository.findByImageUrlIsNull();
            RestTemplate restTemplate = new RestTemplate();
            List<AnimeShow> updatedShows = new ArrayList<>();

            for (AnimeShow show : showsWithoutImages){
                String imageUrl = fetchImageFromJikan(show.getTitle(),restTemplate);
                if (imageUrl != null) {
                    show.setImageUrl(imageUrl);
                    updatedShows.add(show);
                }

                Thread.sleep(5000);
            }
            
            // Batch save all updated shows at once
            if (!updatedShows.isEmpty()) {
                animeShowRepository.saveAll(updatedShows);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // Helper method to query Jikan API for the large_image_url
    private String fetchImageFromJikan(String title,RestTemplate restTemplate){
        try{
             String encodeTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
             String apiUrl = JIKAN_API_BASE + encodeTitle;

             String jsonResponse = restTemplate.getForObject(apiUrl,String.class);
            JSONObject obj = new JSONObject(jsonResponse);

            if(obj.has("data") && obj.getJSONArray("data").length() > 0){
                return obj.getJSONArray("data")
                        .getJSONObject(0)
                        .getJSONObject("images")
                        .getJSONObject("jpg")
                        .getString("large_image_url");
            }
            Thread.sleep(5000);

        }catch (Exception e){
            e.printStackTrace();
        }
        return  null;
    }

    // Called by API to fetch all saved anime from DB as DTO list
    public List<AnimeDownloadInfo> getAllAnimeDownloadInfo() {
        return animeShowRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(a -> new AnimeDownloadInfo(a.getId(), a.getTitle(), a.getDownloadLink(),a.getEpisode(),a.getCreatedAt(),a.getFileName(),a.getImageUrl()))
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
                        a.getFileName(),
                        a.getImageUrl()
                )).collect(Collectors.toList());

        return new AnimeDownloadInfoPage(content, animePage.getTotalElements(),animePage.getTotalPages());
    }

}

