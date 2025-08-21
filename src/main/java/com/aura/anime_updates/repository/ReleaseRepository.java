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
    select 
        r.id as releaseId, 
        r.anime_shows_id as animeShowId,
        ans.title as showTitle,
        r.download_link as releaseDownloadLink,
        r.episode as episode,
        r.file_name as fileName,
        ans.image_url as imgUrl,
        false as tracked
    from releases r
    join anime_shows ans on ans.id = r.anime_shows_id
    order by r.created_at desc
    """,
            countQuery = "select count(*) from releases r",
            nativeQuery = true)
    Page<ReleaseInfoResponse> getReleaseInfo(Pageable pageable);

}


