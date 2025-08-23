package com.aura.anime_updates.features.release.domain.entity;

import com.aura.anime_updates.features.animeShow.domain.entity.AnimeShow;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import com.aura.anime_updates.dto.TrackedReleaseDto;

@Entity
@Table(name = "releases")
@EntityListeners(AuditingEntityListener.class)
public class Release {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String downloadLink;

    @Column()
    private String episode;

    @Column()
    private LocalDateTime releasedDate;

    @Column()
    private String fileName;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_time")
    private LocalDateTime updatedAt;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animeShowsId", nullable = false)
    private AnimeShow animeShow;

    public Release() {}

    public Release(String downloadLink, String episode, LocalDateTime releasedDate, String fileName, AnimeShow animeShow) {
        this.downloadLink = downloadLink;
        this.episode = episode;
        this.releasedDate = releasedDate;
        this.fileName = fileName;
        this.animeShow = animeShow;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public AnimeShow getAnimeShow() {
        return animeShow;
    }

    public void setAnimeShow(AnimeShow animeShow) {
        this.animeShow = animeShow;
    }

    public TrackedReleaseDto toTrackedReleaseDto() {
        return new TrackedReleaseDto(
                this.id,
                this.downloadLink,
                this.episode,
                this.releasedDate,
                this.fileName,
                this.createdAt,
                this.updatedAt
        );
    }
}


