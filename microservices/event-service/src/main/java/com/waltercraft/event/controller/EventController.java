package com.waltercraft.event.controller;

import com.waltercraft.event.dto.EventDetailResponse;
import com.waltercraft.event.dto.EventFiltersResponse;
import com.waltercraft.event.dto.EventSummaryResponse;
import com.waltercraft.event.entity.EventStatus;
import com.waltercraft.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Events", description = "Публичный API ивентов и участников Waltercraft")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Operation(summary = "Получить список активных ивентов с фильтрами и пагинацией")
    public ResponseEntity<Page<EventSummaryResponse>> getEvents(
            @RequestParam(required = false) List<Integer> seasons,
            @RequestParam(name = "organizer", required = false) List<String> organizers,
            @RequestParam(required = false) List<EventStatus> status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        Page<EventSummaryResponse> events = eventService.getEvents(
                seasons, organizers, status, dateFrom, dateTo, search, pageable
        );
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{idOrCode}")
    @Operation(summary = "Получить подробную информацию об ивенте по id или code, включая участников и описание")
    public ResponseEntity<EventDetailResponse> getEventByIdOrCode(@PathVariable String idOrCode) {
        EventDetailResponse event = eventService.getEventByIdOrCode(idOrCode);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/filters")
    @Operation(summary = "Получить доступные опции фильтрации (список сезонов, организаторов, статусов)")
    public ResponseEntity<EventFiltersResponse> getFilters() {
        EventFiltersResponse filters = eventService.getFilters();
        return ResponseEntity.ok(filters);
    }

    @PostMapping("/{id}/participate")
    @Operation(summary = "Записаться участником на ивент")
    public ResponseEntity<EventDetailResponse> participate(
            @PathVariable Long id,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String username = null;
        Long userId = null;

        if (jwt != null) {
            username = jwt.getClaimAsString("preferred_username");
            if (username == null || username.isBlank()) {
                username = jwt.getSubject();
            }
            Object uid = jwt.getClaim("userId");
            if (uid instanceof Number n) {
                userId = n.longValue();
            }
        }

        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body(null);
        }

        EventDetailResponse response = eventService.participate(id, userId, username, notes);
        return ResponseEntity.ok(response);
    }
}
