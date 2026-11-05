package com.waltercraft.auth.dto;

import java.util.UUID;

public record InternalMinecraftLoginRequest(
        UUID uuid,
        String username
) {
}
