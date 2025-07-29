package com.aura.anime_updates.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "anime_shows")
public class AnimeShow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String downloadLink;

    @CreatedDate
    private LocalDateTime createdAt;

    public AnimeShow(){

    }

    public AnimeShow(String title,String downloadLink){
        this.title = title;
        this.downloadLink = downloadLink;
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







}
