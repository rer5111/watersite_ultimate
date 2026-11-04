package com.waltercraft.chat.dto;

import java.time.Instant;

public record ChatMessageResponse(long id, String user, String message, Origin origin, Instant sentAt) {

    public enum Origin {
        WEB,
        DISCORD,
        MINECRAFT
    }
}
