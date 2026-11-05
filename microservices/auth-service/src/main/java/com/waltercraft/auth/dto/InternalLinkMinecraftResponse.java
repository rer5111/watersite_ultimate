package com.waltercraft.auth.dto;

import java.util.UUID;

public record InternalLinkMinecraftResponse(
        boolean success,
        long userId,
        UUID uuid,
        String username,
        String message
) {
}
