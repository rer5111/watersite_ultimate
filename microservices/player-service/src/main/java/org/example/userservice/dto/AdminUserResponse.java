package org.example.userservice.dto;

import java.time.LocalDate;

public record AdminUserResponse(
        long id,
        String username,
        String displayName,
        String email,
        String role,
        String status,
        LocalDate registeredAt
) {
}
