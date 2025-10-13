package com.aura.anime_updates.core;

import lombok.Builder;

@Builder
public record ErrorResponse (
    int status,
    String error,
    String code,
    String message
) { }
