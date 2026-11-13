package com.waltercraft.event.dto;

import com.waltercraft.event.entity.EventStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EventCreateRequest(
        @NotBlank @Size(max = 64) String code,
        @NotBlank @Size(max = 120) String title,
        @NotBlank String description,
        @Size(max = 500) String shortDescription,
        @Size(max = 255) String image,
        @NotNull @Positive Integer seasonNumber,
        @NotBlank @Size(max = 120) String organizer,
        @NotNull EventStatus status,
        LocalDate startDate,
        LocalDate endDate,
        Integer displayOrder,
        Boolean active
) {
}
