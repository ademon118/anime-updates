package com.aura.anime_updates.repository;

import com.aura.anime_updates.domain.Release;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReleaseRepository extends JpaRepository<Release, Long> {
    boolean existsByDownloadLink(String downloadLink);
}


