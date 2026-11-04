package com.waltercraft.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApplicationCreateRequest(
        @NotBlank @Size(max = 4000) String text
) {
}
