package com.aura.anime_updates.features.release.domain.repository;

import com.aura.anime_updates.features.release.api.response.ReleaseInfoResponse;
import com.aura.anime_updates.features.release.domain.dto.ReleaseInfoDTO;
import com.aura.anime_updates.features.release.domain.entity.Release;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

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
            r.created_at as releasedDate,
            CASE
                WHEN :userId IS NOT NULL
                    AND EXISTS (
                        SELECT 1 FROM user_tracked_shows t
                        WHERE ans.id = t.anime_show_id AND t.user_id = :userId
                        )
                THEN TRUE
                ELSE FALSE
            END AS tracked
        FROM releases r
        JOIN anime_shows ans ON ans.id = r.anime_shows_id
        ORDER BY r.created_at DESC
        """,
            countQuery = "SELECT COUNT(*) FROM releases r",
            nativeQuery = true)
    Page<ReleaseInfoDTO> getReleaseInfo(Pageable pageable, @Param("userId") Long userId);

    @Query(value = """
    SELECT 
        r.id AS releaseId, 
        r.anime_shows_id AS animeShowId,
        ans.title AS showTitle,
        r.download_link AS releaseDownloadLink,
        r.episode AS episode,
        r.file_name AS fileName,
        ans.image_url AS imgUrl,
        r.created_at as releasedDate
    FROM releases r
    JOIN anime_shows ans ON ans.id = r.anime_shows_id
    WHERE r.id = :id
    """,
            nativeQuery = true)
    Optional<ReleaseInfoResponse> findReleaseById(@Param("id") Long id);

}


