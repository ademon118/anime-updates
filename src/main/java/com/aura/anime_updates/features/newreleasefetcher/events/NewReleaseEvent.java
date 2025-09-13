package com.aura.anime_updates.features.newreleasefetcher.events;

import org.springframework.context.ApplicationEvent;

public class NewReleaseEvent extends ApplicationEvent {

    private final Long releaseId;

    public NewReleaseEvent(Object source, Long releaseId) {
        super(source);
        this.releaseId = releaseId;
    }

    public Long getReleaseId() {
        return releaseId;
    }
}
