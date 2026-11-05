package com.waltercraft.event.dto;

import com.waltercraft.event.entity.Event;
import com.waltercraft.event.entity.EventStatus;
import java.time.Instant;

public record EventResponse(
        Long id,
        String code,
        String title,
        String description,
        String startDate,
        String endDate,
        String image,
        int displayOrder,
        Instant startsAt,
        boolean active,
        String organizer,
        EventStatus status,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getCode(),
                event.getTitle(),
                event.getDescription(),
                event.getStartDate(),
                event.getEndDate(),
                event.getImage(),
                event.getDisplayOrder(),
                event.getStartsAt(),
                event.isActive(),
                event.getOrganizer(),
                event.getStatus(),
                event.getVersion(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
