package org.example.userservice.dto;

import java.util.UUID;

public record LinkMinecraftResponse(
        boolean success,
        long userId,
        UUID uuid,
        String username,
        String message
) {
    public static LinkMinecraftResponse success(long userId, UUID uuid, String username) {
        return new LinkMinecraftResponse(true, userId, uuid, username, null);
    }

    public static LinkMinecraftResponse failure(String message) {
        return new LinkMinecraftResponse(false, 0L, null, null, message);
    }
}
