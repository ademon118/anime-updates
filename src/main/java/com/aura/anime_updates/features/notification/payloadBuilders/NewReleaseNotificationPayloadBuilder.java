package com.aura.anime_updates.features.notification.payloadBuilders;

import com.aura.anime_updates.features.release.api.response.ReleaseInfoResponse;
import com.aura.anime_updates.features.release.domain.entity.Release;
import com.aura.anime_updates.features.release.domain.mapper.ReleaseMapper;
import com.aura.anime_updates.features.release.domain.repository.ReleaseRepository;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NewReleaseNotificationPayloadBuilder {

    private final ReleaseRepository releaseRepository;
    private final ReleaseMapper releaseMapper;

    public Map<String, String> buildReleaseNotificationPayload(Long releaseId) {
        ReleaseInfoResponse newRelease = releaseMapper.toResponse(releaseRepository.findReleaseById(releaseId).get());
        Notification notification = Notification.builder()
                .setTitle(newRelease.showTitle())
                .setBody("Episode " + newRelease.episode() + " Released!")
                .setImage(newRelease.imgUrl())
                .build();

        return releaseMapper.toMap(newRelease);
    }
}
