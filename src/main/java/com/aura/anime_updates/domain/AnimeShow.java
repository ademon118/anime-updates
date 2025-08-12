package com.aura.anime_updates.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "anime_shows")
@EntityListeners(AuditingEntityListener.class)
public class AnimeShow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String downloadLink;

    @Column()
    private String episode;

    @Column()
    private LocalDateTime releasedDate;

    @Column()
    private String fileName;

    @Column()
    private String imageUrl;

    @CreatedDate
    @Column(name = "created_at",updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_time")
    private LocalDateTime updatedAt;

    public AnimeShow(){

    }

    public AnimeShow(String title,String downloadLink,String episode,LocalDateTime releasedDate,String fileName,String imageUrl){
        this.title = title;
        this.downloadLink = downloadLink;
        this.episode = episode;
        this.releasedDate = releasedDate;
        this.fileName = fileName;
        this.imageUrl = imageUrl;
    }

    public Long getId(){
        return this.id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public String getTitle(){
        return this.title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getDownloadLink(){
        return this.downloadLink;
    }

    public void setDownloadLink(String downloadLink){
        this.downloadLink = downloadLink;
    }

    public String getEpisode(){
        return this.episode;
    }

    public void setEpisode(String episode){
        this.episode = episode;
    }

    public LocalDateTime getReleasedDate(){
        return this.releasedDate;
    }

    public void setReleasedDate(LocalDateTime releasedDate){
        this.releasedDate = releasedDate;
    }


    public  LocalDateTime getCreatedAt(){
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt(){
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
    }

    public String getFileName(){
        return this.fileName;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }

    public String getImageUrl(){
        return this.imageUrl;
    }

    public void setImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }








}
