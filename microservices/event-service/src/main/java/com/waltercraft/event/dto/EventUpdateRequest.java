package com.waltercraft.event.dto;

import com.waltercraft.event.entity.EventStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EventUpdateRequest(
        @Size(max = 120) String title,
        String description,
        @Size(max = 500) String shortDescription,
        @Size(max = 255) String image,
        Integer seasonNumber,
        @Size(max = 120) String organizer,
        EventStatus status,
        LocalDate startDate,
        LocalDate endDate,
        Integer displayOrder,
        Boolean active
) {
}
