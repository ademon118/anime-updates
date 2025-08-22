package com.aura.anime_updates.services;

import com.aura.anime_updates.api.response.ReleaseInfoResponse;
import com.aura.anime_updates.domain.AnimeShow;
import com.aura.anime_updates.domain.Release;
import com.aura.anime_updates.domain.User;
import com.aura.anime_updates.dto.AnimeDownloadInfo;
import com.aura.anime_updates.dto.AnimeDownloadInfoPage;
import com.aura.anime_updates.repository.AnimeShowRepository;
import com.aura.anime_updates.repository.ReleaseRepository;
import com.aura.anime_updates.repository.UserRepository;
import com.google.firebase.messaging.Notification;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;


import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GetAnimeLinkService {
    private static final String RSS_URL = "https://subsplease.org/rss/?t&r=1080";
    private static final String JIKAN_API_BASE = "https://api.jikan.moe/v4/anime?q=";

    private final AnimeShowRepository animeShowRepository;
    private final ReleaseRepository releaseRepository;
    private final AnimePersistenceService animePersistenceService;
    private final UserRepository userRepository;
    private final FcmNotificationService notificationService;
    private final TrackingService trackingService;

    public GetAnimeLinkService(final AnimeShowRepository animeShowRepository,
                               final ReleaseRepository releaseRepository,
                               AnimePersistenceService animePersistenceService,
                               UserRepository userRepository,
                               final FcmNotificationService notificationService,
                               final TrackingService trackingService){
        this.animeShowRepository = animeShowRepository;
        this.releaseRepository = releaseRepository;
        this.animePersistenceService = animePersistenceService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.trackingService = trackingService;
    }


    //Fetches RSS data, queries Jikan API for images, and stores new anime and releases
    @Transactional
    public void fetchAndSaveNewAnimeShows() {
        try {
            URL feedSource = new URL(RSS_URL);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedSource));

            Map<String, AnimeShow> titleToShow = new HashMap<>();
            List<Release> releasesToSave = new ArrayList<>();
            RestTemplate restTemplate = new RestTemplate();

            for (SyndEntry entry : feed.getEntries()) {
                String rawTitle = entry.getTitle();
                String link = entry.getLink();
                String category = entry.getCategories().isEmpty() ? null : entry.getCategories().get(0).getName();
                LocalDateTime releasedDate = entry.getPublishedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                String episode = extractEpisodeNumber(rawTitle);
                String filename = rawTitle.replaceAll("\\.mkv$", "");

                if( episode == null ||  category == null || !link.contains("nyaa.si")){
                    continue;
                }

                String cleanTitle = category.replaceAll("\\s*-\\s*1080", "").trim();

                if (releaseRepository.existsByDownloadLink(link)) {
                    continue;
                }

                AnimeShow show = titleToShow.computeIfAbsent(cleanTitle, t -> {
                    AnimeShow existing = animeShowRepository.findByTitle(t);
                    if (existing != null) {
                        return existing;
                    }
                    String imageUrl = fetchImageFromJikan(t, restTemplate);
                    return new AnimeShow(t, imageUrl);
                });

                Release release = new Release(link, episode, releasedDate, filename, show);
                releasesToSave.add(release);

                Notification notification = Notification.builder()
                        .setTitle(show.getTitle())
                        .setBody("Episode " + release.getEpisode() + " Released!")
                        .setImage(show.getImageUrl())
                        .build();

                notificationService.sendNotificationToAllDevicesOfUsers(show.getTrackingUsers(), notification);
                Thread.sleep(5000);
            }

            if (!titleToShow.isEmpty()) {
                animePersistenceService.saveAllShows(new ArrayList<>(titleToShow.values()));
                System.out.println("Saved shows: " + titleToShow.size());
            }
            if (!releasesToSave.isEmpty()) {
                animePersistenceService.saveAllReleases(releasesToSave);
                System.out.println("Saved releases: " + releasesToSave.size());
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
            
            if (!updatedShows.isEmpty()) {
                animeShowRepository.saveAll(updatedShows);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

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

    public List<AnimeDownloadInfo> getAllAnimeDownloadInfo() {
        return getAllAnimeDownloadInfo(null);
    }

    public List<AnimeDownloadInfo> getAllAnimeDownloadInfo(Long userId) {
        Set<Long> trackedShowIds = new HashSet<>();
        
        if (userId != null) {
            try {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    trackedShowIds.addAll(user.getTrackedShows().stream()
                            .map(AnimeShow::getId)
                            .collect(Collectors.toSet()));
                }
            } catch (Exception e) {
                // Log error but continue without tracking info
                System.err.println("Error getting tracking info for user " + userId + ": " + e.getMessage());
            }
        }

        final Set<Long> finalTrackedShowIds = trackedShowIds;

        return animeShowRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .flatMap(show -> show.getReleases().stream().map(r -> new AnimeDownloadInfo(
                        r.getId(),
                        show.getId(),
                        show.getTitle(),
                        r.getDownloadLink(),
                        r.getEpisode(),
                        r.getReleasedDate(),
                        r.getFileName(),
                        show.getImageUrl(),
                        finalTrackedShowIds.contains(show.getId())
                )))
                .sorted((a,b) -> b.getReleasedDate().compareTo(a.getReleasedDate()))
                .collect(Collectors.toList());
    }

    private String extractEpisodeNumber(String title){
        Pattern pattern = Pattern.compile("\\-\\s*(\\d{2})\\s*\\(");
        Matcher matcher = pattern.matcher(title);
        return matcher.find()?matcher.group(1) : null;
    }

    public AnimeDownloadInfoPage getAllAnimeDownloadInfoPaginated(int page, int size) {
        return getAllAnimeDownloadInfoPaginated(page, size, null);
    }

    public AnimeDownloadInfoPage getAllAnimeDownloadInfoPaginated(int page, int size, Long userId) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<AnimeShow> animePage = animeShowRepository.findAllByOrderByCreatedAtDesc(pageable);
        return toDtoPage(animePage, userId);
    }

    public AnimeDownloadInfoPage searchByTitle(String title, int page, int size) {
        return searchByTitle(title, page, size, null);
    }

    public AnimeDownloadInfoPage searchByTitle(String title, int page, int size, Long userId) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<AnimeShow> animePage = animeShowRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(title, pageable);
        return toDtoPage(animePage, userId);
    }

    private AnimeDownloadInfoPage toDtoPage(Page<AnimeShow> animePage, Long userId) {
        Set<Long> trackedShowIds = new HashSet<>();
        
        if (userId != null) {
            try {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    trackedShowIds.addAll(user.getTrackedShows().stream()
                            .map(AnimeShow::getId)
                            .collect(Collectors.toSet()));
                }
            } catch (Exception e) {
                // Log error but continue without tracking info
                System.err.println("Error getting tracking info for user " + userId + ": " + e.getMessage());
            }
        }

        final Set<Long> finalTrackedShowIds = trackedShowIds;

        List<AnimeDownloadInfo> content = animePage.getContent().stream()
                .flatMap(show -> show.getReleases().stream().map(r -> new AnimeDownloadInfo(
                        r.getId(),
                        show.getId(),
                        show.getTitle(),
                        r.getDownloadLink(),
                        r.getEpisode(),
                        r.getReleasedDate(),
                        r.getFileName(),
                        show.getImageUrl(),
                        finalTrackedShowIds.contains(show.getId())
                )))
                .sorted((a,b) -> b.getReleasedDate().compareTo(a.getReleasedDate()))
                .collect(Collectors.toList());

        return new AnimeDownloadInfoPage(content, animePage.getTotalElements(), animePage.getTotalPages());
    }

    public Page<ReleaseInfoResponse> getAllReleaseInfo(Integer page, Integer size){
        Pageable pageable = PageRequest.of(page, size);
        return releaseRepository.getReleaseInfo(pageable);
    }

}

 