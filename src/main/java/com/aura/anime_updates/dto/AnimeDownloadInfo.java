package com.aura.anime_updates.dto;

import java.time.LocalDateTime;

public class AnimeDownloadInfo {
    private  String title;
    private  String downloadlink;
    private  String episode;
    private LocalDateTime releasedDate;

    public AnimeDownloadInfo(String title, String downloadlink,String episode, LocalDateTime releasedDate){
        this.title = title;
        this.downloadlink = downloadlink;
        this.episode = episode;
        this.releasedDate = releasedDate;
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
