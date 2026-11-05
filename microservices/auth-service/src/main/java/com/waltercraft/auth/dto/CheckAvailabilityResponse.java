package com.waltercraft.auth.dto;

public record CheckAvailabilityResponse(
        boolean available,
        String message,
        String field
) {
}
