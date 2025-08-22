package com.aura.anime_updates.repository;

import com.aura.anime_updates.api.response.ReleaseInfoResponse;
import com.aura.anime_updates.domain.Release;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReleaseRepository extends JpaRepository<Release, Long> {
    boolean existsByDownloadLink(String downloadLink);

    @Query(value = """
    SELECT 
        r.id AS releaseId, 
        r.anime_shows_id AS animeShowId,
        ans.title AS showTitle,
        r.download_link AS releaseDownloadLink,
        r.episode AS episode,
        r.file_name AS fileName,
        ans.image_url AS imgUrl,
        false As tracked
    FROM releases r
    JOIN anime_shows ans ON ans.id = r.anime_shows_id
    ORDER BY r.created_at DESC 
    """,
            countQuery = "SELECT COUNT(*) FROM releases r",
            nativeQuery = true)
    Page<ReleaseInfoResponse> getReleaseInfo(Pageable pageable);

}


