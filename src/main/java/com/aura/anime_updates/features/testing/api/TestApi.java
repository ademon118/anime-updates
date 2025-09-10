package com.aura.anime_updates.features.testing.api;

import com.aura.anime_updates.features.newreleasefetcher.events.NewReleaseEvent;
import com.aura.anime_updates.features.release.domain.mapper.ReleaseMapper;
import com.aura.anime_updates.features.release.domain.repository.ReleaseRepository;
import com.aura.anime_updates.features.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestApi {

    private final String PASSWORD_ENCRYPTED = "$2a$12$LCAtczpKqtkL66QJkjvqGeSdMxhirWu.muV64qUa/H7J3PUMKAp0y";
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserRepository userRepository;
    private final ReleaseRepository releaseRepository;
    private final ReleaseMapper releaseMapper;
    private final ApplicationEventPublisher publisher;

    @GetMapping("/noti")
    public String sendNoti(@RequestParam String password) {
        if(!passwordEncoder.matches(password, PASSWORD_ENCRYPTED)) {
            return null;
        }

        Long releaseId = 44L;
        String episode = "21";
        Long animeShowId = 41L;
        String imageUrl = "https://cdn.myanimelist.net/images/anime/4/19644l.jpg";

        publisher.publishEvent(new NewReleaseEvent(this, releaseId, episode, animeShowId, imageUrl));

        return "Test Complete";
    }
}