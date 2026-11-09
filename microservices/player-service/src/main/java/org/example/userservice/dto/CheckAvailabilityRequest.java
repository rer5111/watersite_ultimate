package org.example.userservice.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

public record CheckAvailabilityRequest(
        @Email String email,
        @Size(min = 3, max = 32) String username
) {
    public CheckAvailabilityRequest {
        if (email != null && email.isBlank()) {
            email = null;
        }
        if (username != null && username.isBlank()) {
            username = null;
        }
    }
}
