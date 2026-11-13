package com.waltercraft.season.dto;

import com.waltercraft.season.entity.Season;
import com.waltercraft.season.entity.SeasonStatus;
import java.time.Instant;

public record SeasonResponse(
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
        SeasonStatus status,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
    public static SeasonResponse from(Season season) {
        return new SeasonResponse(
                season.getId(),
                season.getCode(),
                season.getTitle(),
                season.getDescription(),
                season.getStartDate(),
                season.getEndDate(),
                season.getImage(),
                season.getDisplayOrder(),
                season.getStartsAt(),
                season.isActive(),
                season.getOrganizer(),
                season.getStatus(),
                season.getVersion(),
                season.getCreatedAt(),
                season.getUpdatedAt()
        );
    }
}
