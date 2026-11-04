package com.waltercraft.application.dto;

import com.waltercraft.application.entity.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ApplicationUpdateRequest(
        @NotBlank @Size(max = 4000) String text,
        @NotNull ApplicationStatus status,
        @NotNull @PositiveOrZero Long version
) {
}
