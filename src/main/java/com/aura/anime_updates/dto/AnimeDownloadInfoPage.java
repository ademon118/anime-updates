package com.aura.anime_updates.dto;

import java.util.List;

public class AnimeDownloadInfoPage {
    private List<AnimeDownloadInfo> content;
    private  long totalElements;
    private  int totalPages;

    public AnimeDownloadInfoPage(List<AnimeDownloadInfo> content,long totalElements,int totalPages){
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<AnimeDownloadInfo> getContent() { return content; }

    public long getTotalElements() { return totalElements; }

    public int getTotalPages() { return totalPages; }
}
