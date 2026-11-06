package com.waltercraft.event.controller;

import com.waltercraft.event.dto.EventCreateRequest;
import com.waltercraft.event.dto.EventResponse;
import com.waltercraft.event.dto.EventUpdateRequest;
import com.waltercraft.event.service.EventService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/events")
    public ResponseEntity<PagedModel<EventResponse>> listEvents(
            @ParameterObject @PageableDefault(size = 50, sort = {"displayOrder", "id"}) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic())
                .body(new PagedModel<>(eventService.listPublic(pageable)));
    }

    @GetMapping("/events/{id}")
    public EventResponse getEvent(@PathVariable Long id) {
        return eventService.get(id);
    }

    @GetMapping("/admin/events")
    public PagedModel<EventResponse> listAllEvents(
            @ParameterObject @PageableDefault(size = 50, sort = {"displayOrder", "id"}) Pageable pageable) {
        return new PagedModel<>(eventService.listAll(pageable));
    }

    @PostMapping("/admin/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(@Valid @RequestBody EventCreateRequest request) {
        return eventService.create(request);
    }

    @PutMapping("/admin/events/{id}")
    public EventResponse updateEvent(@PathVariable Long id, @Valid @RequestBody EventUpdateRequest request) {
        return eventService.update(id, request);
    }

    @DeleteMapping("/admin/events/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Long id) {
        eventService.delete(id);
    }
}
