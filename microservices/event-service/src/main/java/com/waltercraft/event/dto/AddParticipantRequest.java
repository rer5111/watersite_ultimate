package com.waltercraft.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddParticipantRequest(
        @NotBlank @Size(max = 64) String username,
        @Size(max = 32) String role,
        @Size(max = 255) String notes
) {
}
