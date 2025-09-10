package com.aura.anime_updates.features.newreleasefetcher.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NewReleaseEvent extends ApplicationEvent {

    private final Long releaseId;
    private final String episode;
    private final Long animeShowId;
    private final String imgUrl;

    public NewReleaseEvent(Object source, Long releaseId, String episode, Long animeShowId, String imgUrl) {
        super(source);
        this.releaseId = releaseId;
        this.episode = episode;
        this.animeShowId = animeShowId;
        this.imgUrl = imgUrl;
    }
}
