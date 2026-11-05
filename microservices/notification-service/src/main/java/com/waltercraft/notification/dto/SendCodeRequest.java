package com.waltercraft.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendCodeRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 32) String purpose
) {
}
