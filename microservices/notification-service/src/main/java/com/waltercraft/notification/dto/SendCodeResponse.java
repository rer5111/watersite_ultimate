package com.waltercraft.notification.dto;

public record SendCodeResponse(
        String status,
        int cooldownSeconds,
        int expiresInSeconds
) {
}
