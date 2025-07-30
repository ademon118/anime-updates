package com.aura.anime_updates.repository;

import com.aura.anime_updates.domain.AnimeShow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimeShowRepository extends JpaRepository<AnimeShow, Long> {

    AnimeShow findByTitle(String title);

    boolean existsByDownloadLink(String downloadLink);

    List<AnimeShow> findAllByOrderByReleasedDateDesc();


}
