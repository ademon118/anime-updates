package com.aura.anime_updates.domain;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String userName;
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_tracked_shows",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "anime_show_id")
    )
    private Set<AnimeShow> trackedShows = new HashSet<>();

    public User(){

    }

    public User(Long id,String userName,String password){
        this.id = id;
        this.userName = userName;
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<AnimeShow> getTrackedShows() {
        return trackedShows;
    }

    public void setTrackedShows(Set<AnimeShow> trackedShows) {
        this.trackedShows = trackedShows;
    }

    public void trackShow(AnimeShow show) {
        this.trackedShows.add(show);
    }

    public void untrackShow(AnimeShow show) {
        this.trackedShows.remove(show);
    }
}
