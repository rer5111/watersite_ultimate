package org.example.userservice.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public record CheckAvailabilityRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 3, max = 32) String username
) {
}
