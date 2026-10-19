package com.aura.anime_updates.features.newreleasefetcher.imagefetcher;

import com.aura.anime_updates.features.animeShow.domain.entity.AnimeShow;
import com.aura.anime_updates.features.animeShow.domain.repository.AnimeShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageFetchingService {

    private final String IMAGE_API = "https://kitsu.io/api/edge/anime?filter[text]={title}&filter[status]=current,finished&sort=-startDate";
    private final RestTemplate restTemplate = new RestTemplate();
    private final AnimeShowRepository animeShowRepository;

    public String fetchImageForAnimeShow(String title) {
        try {
            String apiResponse = restTemplate.getForObject(IMAGE_API, String.class, title);
            JSONObject jsonObject = new JSONObject(apiResponse);

            if(jsonObject.has("data") && jsonObject.getJSONArray("data").length() > 0) {
                return jsonObject.getJSONArray("data")
                        .getJSONObject(0)
                        .getJSONObject("attributes")
                        .getJSONObject("posterImage")
                        .getString("original");
            }
            return null;
        } catch(Exception e) {
            log.error("Failed to fetch image from Jikan with error : {}", e.getMessage());
            return null;
        }
    }

    public void backfillMissingImages(){
        try {
            List<AnimeShow> showsWithoutImages = animeShowRepository.findByImageUrlIsNull();
            List<AnimeShow> updatedShows = new ArrayList<>();

            for (AnimeShow show : showsWithoutImages){
                String imageUrl = fetchImageForAnimeShow(show.getTitle());
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
}
