package com.aura.anime_updates.dto;

import java.time.LocalDateTime;

public class AnimeDownloadInfo {
    private Long id;
    private  String title;
    private  String downloadlink;
    private  String episode;
    private LocalDateTime releasedDate;
    private String fileName;

    public AnimeDownloadInfo(Long id, String title, String downloadlink,String episode, LocalDateTime releasedDate,String fileName){
        this.id = id;
        this.title = title;
        this.downloadlink = downloadlink;
        this.episode = episode;
        this.releasedDate = releasedDate;
        this.fileName = fileName;
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

    public String getFileName(){return fileName;}


}
