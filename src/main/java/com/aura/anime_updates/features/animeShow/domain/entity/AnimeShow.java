package com.aura.anime_updates.features.animeShow.domain.entity;

import com.aura.anime_updates.features.release.domain.entity.Release;
import com.aura.anime_updates.features.user.domain.entity.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "anime_shows")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class AnimeShow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String title;

    @Column()
    private String imageUrl;

    @CreatedDate
    @Column(name = "created_at",updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_time")
    private LocalDateTime updatedAt;

    @JsonManagedReference
    @OneToMany(mappedBy = "animeShow", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Release> releases = new ArrayList<>();

    @JsonManagedReference
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_tracked_shows",
            joinColumns = @JoinColumn(name = "anime_show_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Getter
    private List<User> trackingUsers = new ArrayList<>();

    public AnimeShow(){

    }

    public AnimeShow(String title,String imageUrl){
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public void addTracker(User user) {
        this.trackingUsers.add(user);
    }

    public void removeTracker(User user) {
        this.trackingUsers.remove(user);
    }
}
