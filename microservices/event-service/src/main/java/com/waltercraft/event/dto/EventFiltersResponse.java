package com.waltercraft.event.dto;

import com.waltercraft.event.entity.EventStatus;

import java.time.LocalDate;
import java.util.List;

public record EventFiltersResponse(
        List<Integer> seasons,
        List<String> organizers,
        List<EventStatus> statuses,
        LocalDate minDate,
        LocalDate maxDate
) {
}
