package com.aura.anime_updates.features.animeShow.domain.repository;

import com.aura.anime_updates.features.animeShow.api.response.AnimeShowResponse;
import com.aura.anime_updates.features.animeShow.domain.entity.AnimeShow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnimeShowRepository extends JpaRepository<AnimeShow, Long> {

    Optional<AnimeShow> findByTitle(String title);

    List<AnimeShow> findByImageUrlIsNull();

    @Query(value = """
            SELECT
                sh.id,
                sh.title,
                sh.image_url,
                MAX(r.created_at) AS latest_released_time
            FROM anime_shows sh
            JOIN releases r ON sh.id = r.anime_show_id
            JOIN user_tracked_shows tr ON sh.id = tr.anime_show_id
            WHERE tr.user_id = :userId
            GROUP BY sh.id
            ORDER BY latest_released_time DESC;
            """, nativeQuery = true)
    Page<AnimeShowResponse> getAllTrackedShowsByUser(Pageable pageable, @Param("userId") Long userId);
}
