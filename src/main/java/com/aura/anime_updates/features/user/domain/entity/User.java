package com.aura.anime_updates.features.user.domain.entity;

import com.aura.anime_updates.features.animeShow.domain.entity.AnimeShow;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import com.aura.anime_updates.dto.TrackedShowDto;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userName;

    private String password;

    @JsonManagedReference
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_tracked_shows",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "anime_show_id")
    )
    private Set<AnimeShow> trackedShows = new HashSet<>();


    public void setTrackedShows(Set<AnimeShow> trackedShows) {
        this.trackedShows = trackedShows;
    }

    public void trackShow(AnimeShow show) {
        this.trackedShows.add(show);
    }

    public void untrackShow(AnimeShow show) {
        this.trackedShows.remove(show);
    }

    public Set<TrackedShowDto> getTrackedShowsAsDtos() {
        return this.trackedShows.stream()
                .map(show -> new TrackedShowDto(
                        show.getId(),
                        show.getTitle(),
                        show.getImageUrl(),
                        show.getCreatedAt(),
                        show.getUpdatedAt()
                ))
                .sorted((a,b)->b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toSet());
    }
}
