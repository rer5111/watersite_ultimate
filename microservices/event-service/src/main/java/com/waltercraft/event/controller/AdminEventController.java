package com.waltercraft.event.controller;

import com.waltercraft.event.dto.AddParticipantRequest;
import com.waltercraft.event.dto.EventCreateRequest;
import com.waltercraft.event.dto.EventDetailResponse;
import com.waltercraft.event.dto.EventParticipantResponse;
import com.waltercraft.event.dto.EventSummaryResponse;
import com.waltercraft.event.dto.EventUpdateRequest;
import com.waltercraft.event.entity.EventStatus;
import com.waltercraft.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/events")
@Tag(name = "Admin Events", description = "Управление ивентами и участниками (для администраторов)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminEventController {

    private final EventService eventService;

    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Operation(summary = "Получить список всех ивентов (включая неактивные) для админ-панели")
    public ResponseEntity<Page<EventSummaryResponse>> getAllEvents(
            @RequestParam(required = false) List<Integer> seasons,
            @RequestParam(required = false) String organizer,
            @RequestParam(required = false) List<EventStatus> status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        Page<EventSummaryResponse> events = eventService.getAllEventsAdmin(
                seasons, organizer, status, dateFrom, dateTo, search, pageable
        );
        return ResponseEntity.ok(events);
    }

    @PostMapping
    @Operation(summary = "Создать новый ивент")
    public ResponseEntity<EventDetailResponse> createEvent(@Valid @RequestBody EventCreateRequest request) {
        EventDetailResponse created = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить существующий ивент")
    public ResponseEntity<EventDetailResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateRequest request
    ) {
        EventDetailResponse updated = eventService.updateEvent(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить ивент")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/participants")
    @Operation(summary = "Добавить участника к ивенту вручную")
    public ResponseEntity<EventParticipantResponse> addParticipant(
            @PathVariable Long id,
            @Valid @RequestBody AddParticipantRequest request
    ) {
        EventParticipantResponse participant = eventService.addParticipant(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(participant);
    }

    @DeleteMapping("/{id}/participants/{participantId}")
    @Operation(summary = "Удалить участника из ивента")
    public ResponseEntity<Void> removeParticipant(
            @PathVariable Long id,
            @PathVariable Long participantId
    ) {
        eventService.removeParticipant(id, participantId);
        return ResponseEntity.noContent().build();
    }
}
