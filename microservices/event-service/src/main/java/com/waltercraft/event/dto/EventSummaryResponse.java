package com.waltercraft.event.dto;

import com.waltercraft.event.entity.Event;
import com.waltercraft.event.entity.EventStatus;

import java.time.LocalDate;

public record EventSummaryResponse(
        Long id,
        String code,
        String title,
        String shortDescription,
        String image,
        Integer seasonNumber,
        String seasonTitle,
        String organizer,
        EventStatus status,
        LocalDate startDate,
        LocalDate endDate,
        String dateRangeFormatted,
        int participantsCount,
        Integer displayOrder,
        Boolean active
) {
    public static EventSummaryResponse from(Event event) {
        String seasonTitle = "%d сезон".formatted(event.getSeasonNumber());
        String dateRangeFormatted = formatDateRange(event.getStartDate(), event.getEndDate());
        int count = event.getParticipants() != null ? event.getParticipants().size() : 0;

        return new EventSummaryResponse(
                event.getId(),
                event.getCode(),
                event.getTitle(),
                event.getShortDescription(),
                event.getImage(),
                event.getSeasonNumber(),
                seasonTitle,
                event.getOrganizer(),
                event.getStatus(),
                event.getStartDate(),
                event.getEndDate(),
                dateRangeFormatted,
                count,
                event.getDisplayOrder(),
                event.getActive()
        );
    }

    private static String formatDateRange(LocalDate start, LocalDate end) {
        if (start == null && end == null) return "";
        if (start != null && end == null) return start.toString();
        if (start == null) return "до " + end.toString();
        return "%s — %s".formatted(start.toString(), end.toString());
    }
}
