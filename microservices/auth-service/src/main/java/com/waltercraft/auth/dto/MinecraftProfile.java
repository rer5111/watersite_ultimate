package com.waltercraft.auth.dto;

import java.util.UUID;

public record MinecraftProfile(
        UUID uuid,
        String username
) {
}
