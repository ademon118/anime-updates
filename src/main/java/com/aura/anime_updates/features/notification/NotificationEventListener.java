package com.aura.anime_updates.features.notification;

import com.aura.anime_updates.features.animeShow.domain.entity.AnimeShow;
import com.aura.anime_updates.features.animeShow.domain.repository.AnimeShowRepository;
import com.aura.anime_updates.features.newreleasefetcher.events.NewReleaseEvent;
import com.aura.anime_updates.features.notification.payloadBuilders.NewReleaseNotificationPayloadBuilder;
import com.aura.anime_updates.features.user.domain.entity.User;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NewReleaseNotificationPayloadBuilder newReleaseNotificationPayloadBuilder;
    private final NotificationService notificationService;
    private final AnimeShowRepository animeShowRepository;

    @EventListener
    public void handleNewReleaseEvent(NewReleaseEvent event) {
        Map<String, String> payload = newReleaseNotificationPayloadBuilder.buildReleaseNotificationPayload(event.getReleaseId());
        AnimeShow newReleasedShow = animeShowRepository.findById(event.getAnimeShowId()).get();
        List<User> usersToSendTo = newReleasedShow.getTrackingUsers();
        Notification notification = Notification.builder()
                .setTitle(newReleasedShow.getTitle())
                .setBody("Episode " + event.getEpisode() + " Released!")
                .setImage(event.getImgUrl())
                .build();
        notificationService.sendNotificationToAllDevicesOfUsers(usersToSendTo, notification, payload);
    }
}
