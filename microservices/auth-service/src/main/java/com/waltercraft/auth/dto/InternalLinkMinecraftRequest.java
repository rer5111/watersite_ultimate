package com.waltercraft.auth.dto;

import java.util.UUID;

public record InternalLinkMinecraftRequest(
        long userId,
        UUID uuid,
        String username
) {
}
