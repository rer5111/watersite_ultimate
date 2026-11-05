package org.example.userservice.dto;

public record CheckAvailabilityResponse(
        boolean available,
        String message,
        String field
) {
    public static CheckAvailabilityResponse success() {
        return new CheckAvailabilityResponse(true, null, null);
    }

    public static CheckAvailabilityResponse taken(String message, String field) {
        return new CheckAvailabilityResponse(false, message, field);
    }
}
