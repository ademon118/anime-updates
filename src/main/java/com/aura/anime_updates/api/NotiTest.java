package com.aura.anime_updates.api;

import com.aura.anime_updates.domain.AnimeShow;
import com.aura.anime_updates.domain.Release;
import com.aura.anime_updates.dto.ApiResponse;
import com.aura.anime_updates.dto.AuthRequest;
import com.aura.anime_updates.dto.AuthResponse;
import com.aura.anime_updates.repository.ReleaseRepository;
import com.aura.anime_updates.services.AuthService;
import com.aura.anime_updates.services.FcmNotificationService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/test/noti")
@RequiredArgsConstructor
public class NotiTest {

    private final FcmNotificationService fcmNotificationService;
    private final ReleaseRepository releaseRepository;

    @GetMapping("send")
    public void noti(){
        Optional<Release> release = releaseRepository.findById(47L);
        Notification notification = Notification.builder()
                .setTitle(release.get().getAnimeShow().getTitle())
                .setBody("Episode " + release.get().getEpisode() + " released!")
                .setImage(release.get().getAnimeShow().getImageUrl())
                .build();
        fcmNotificationService.sendNotificationToAllDevicesOfUsers(release.get().getAnimeShow().getTrackingUsers(), notification);
    }
}