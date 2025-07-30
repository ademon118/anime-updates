package com.aura.anime_updates.repository;

import com.aura.anime_updates.domain.AnimeShow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimeShowRepository extends JpaRepository<AnimeShow, Long> {

    AnimeShow findByTitle(String title);

    boolean existsByDownloadLink(String downloadLink);

}
