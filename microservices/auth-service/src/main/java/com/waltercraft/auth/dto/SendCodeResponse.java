package com.waltercraft.auth.dto;

public record SendCodeResponse(
        String status,
        int cooldownSeconds,
        int expiresInSeconds
) {
}
