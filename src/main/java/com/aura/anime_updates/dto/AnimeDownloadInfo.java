package com.aura.anime_updates.dto;

import java.time.LocalDateTime;

public class AnimeDownloadInfo {
    private Long id;
    private  String title;
    private  String downloadlink;
    private  String episode;
    private LocalDateTime releasedDate;
    private String fileName;
    private String imageUrl;

    public AnimeDownloadInfo(Long id, String title, String downloadlink,String episode, LocalDateTime releasedDate,String fileName,String imageUrl){
        this.id = id;
        this.title = title;
        this.downloadlink = downloadlink;
        this.episode = episode;
        this.releasedDate = releasedDate;
        this.fileName = fileName;
        this.imageUrl = imageUrl;
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


    public String getImageUrl(){
        return imageUrl;
    }



}
