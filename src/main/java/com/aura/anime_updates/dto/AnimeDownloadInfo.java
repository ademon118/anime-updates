package com.aura.anime_updates.dto;

public class AnimeDownloadInfo {
    private  String title;
    private  String downloadlink;

    public AnimeDownloadInfo(String title, String downloadlink){
        this.title = title;
        this.downloadlink = downloadlink;
    }

    public String getTitle(){
        return  title;
    }

    public  String getDownloadlink(){
        return downloadlink;
    }


}
