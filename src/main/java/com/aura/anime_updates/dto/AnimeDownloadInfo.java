package com.aura.anime_updates.dto;

import java.time.LocalDateTime;

public class AnimeDownloadInfo {
    private Long id;
    private Long animeShowsId;
    private String title;
    private String downloadLink;
    private String episode;
    private LocalDateTime releasedDate;
    private String fileName;
    private String imageUrl;
    private boolean isTracked;

    public AnimeDownloadInfo() {}

    public AnimeDownloadInfo(Long id, Long animeShowsId, String title, String downloadLink, String episode, 
                           LocalDateTime releasedDate, String fileName, String imageUrl, boolean isTracked) {
        this.id = id;
        this.animeShowsId = animeShowsId;
        this.title = title;
        this.downloadLink = downloadLink;
        this.episode = episode;
        this.releasedDate = releasedDate;
        this.fileName = fileName;
        this.imageUrl = imageUrl;
        this.isTracked = isTracked;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAnimeShowsId() {
        return animeShowsId;
    }

    public void setAnimeShowsId(Long animeShowsId) {
        this.animeShowsId = animeShowsId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public String getEpisode() {
        return episode;
    }

    public void setEpisode(String episode) {
        this.episode = episode;
    }

    public LocalDateTime getReleasedDate() {
        return releasedDate;
    }

    public void setReleasedDate(LocalDateTime releasedDate) {
        this.releasedDate = releasedDate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isTracked() {
        return isTracked;
    }

    public void setIsTracked(boolean tracked) {
        isTracked = tracked;
    }
}
