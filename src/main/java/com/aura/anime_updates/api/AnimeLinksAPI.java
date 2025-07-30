package com.aura.anime_updates.api;

import com.aura.anime_updates.dto.AnimeDownloadInfo;
import com.aura.anime_updates.services.GetAnimeLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/anime")
public class AnimeLinksAPI {
    @Autowired
    private  GetAnimeLinkService getAnimeLinkService;


    @GetMapping("/downloads")
    public List<AnimeDownloadInfo> getDownloadLinks(){
        return getAnimeLinkService.getAllAnimeDownloadInfo();
    }



}
