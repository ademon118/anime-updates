package com.aura.anime_updates.features.animeShow.domain.repository;

import com.aura.anime_updates.features.animeShow.domain.entity.AnimeShow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimeShowRepository extends JpaRepository<AnimeShow, Long> {

    AnimeShow findByTitle(String title);

    //All list without pagination
    List<AnimeShow> findAllByOrderByCreatedAtDesc();

    //For pagination
    Page<AnimeShow> findAllByOrderByCreatedAtDesc(Pageable pageable);

    //For paginated search by title (partial match, case-insensitive)
    Page<AnimeShow> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title, Pageable pageable);

    List<AnimeShow> findByImageUrlIsNull();
}
