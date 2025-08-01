package com.aura.anime_updates.dto;

import java.time.LocalDateTime;

public class AnimeDownloadInfo {
    private Long id;
    private  String title;
    private  String downloadlink;
    private  String episode;
    private LocalDateTime releasedDate;

    public AnimeDownloadInfo(Long id, String title, String downloadlink,String episode, LocalDateTime releasedDate){
        this.id = id;
        this.title = title;
        this.downloadlink = downloadlink;
        this.episode = episode;
        this.releasedDate = releasedDate;
    }

    public Long getId() {
        return this.id;
    }

    public String getTitle(){
        return  title;
    }

    public  String getDownloadlink(){
        return downloadlink;
    }

    public String getEpisode(){
        return episode;
    }

    public LocalDateTime getReleasedDate(){
        return releasedDate;
    }


}
