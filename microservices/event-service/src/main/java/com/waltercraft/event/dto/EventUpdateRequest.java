package com.waltercraft.event.dto;

import com.waltercraft.event.entity.EventStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record EventUpdateRequest(
        @NotBlank @Size(max = 64) String code,
        @NotBlank @Size(max = 120) String title,
        @NotBlank @Size(max = 1000) String description,
        @NotBlank @Size(max = 32) String startDate,
        @NotBlank @Size(max = 32) String endDate,
        @NotBlank @Size(max = 255) String image,
        @NotNull @PositiveOrZero Integer displayOrder,
        Instant startsAt,
        @Size(max = 120) String organizer,
        EventStatus status,
        Boolean active,
        @NotNull Long version
) {
}
