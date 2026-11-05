package com.waltercraft.event.service;

import com.waltercraft.event.dto.EventCreateRequest;
import com.waltercraft.event.dto.EventResponse;
import com.waltercraft.event.dto.EventUpdateRequest;
import com.waltercraft.event.entity.Event;
import com.waltercraft.event.entity.EventStatus;
import com.waltercraft.event.exception.ConflictException;
import com.waltercraft.event.exception.ResourceNotFoundException;
import com.waltercraft.event.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Page<EventResponse> listPublic(Pageable pageable) {
        return eventRepository.findAllByActiveTrue(pageable).map(EventResponse::from);
    }

    public Page<EventResponse> listAll(Pageable pageable) {
        return eventRepository.findAll(pageable).map(EventResponse::from);
    }

    public EventResponse get(Long id) {
        return EventResponse.from(findEvent(id));
    }

    @Transactional
    public EventResponse create(EventCreateRequest request) {
        if (eventRepository.existsByCode(request.code())) {
            throw new ConflictException("Событие с кодом '" + request.code() + "' уже существует.");
        }
        EventStatus status = request.status() != null
                ? request.status()
                : (Boolean.FALSE.equals(request.active()) ? EventStatus.DRAFT : EventStatus.PUBLISHED);
        Event event = new Event(
                request.code(),
                request.title(),
                request.description(),
                request.startDate(),
                request.endDate(),
                request.image(),
                request.displayOrder(),
                request.organizer() == null ? "" : request.organizer().trim(),
                status
        );
        event.setStartsAt(request.startsAt());
        return EventResponse.from(eventRepository.saveAndFlush(event));
    }

    @Transactional
    public EventResponse update(Long id, EventUpdateRequest request) {
        Event event = findEvent(id);
        if (!event.getVersion().equals(request.version())) {
            throw new ConflictException("Событие уже было изменено другим процессом. Обновите страницу и повторите попытку.");
        }
        if (eventRepository.existsByCodeAndIdNot(request.code(), id)) {
            throw new ConflictException("Код события '" + request.code() + "' уже используется другим событием.");
        }
        EventStatus status = request.status() != null
                ? request.status()
                : (Boolean.FALSE.equals(request.active()) ? EventStatus.DRAFT : EventStatus.PUBLISHED);
        event.replace(
                request.code(),
                request.title(),
                request.description(),
                request.startDate(),
                request.endDate(),
                request.image(),
                request.displayOrder(),
                request.organizer() == null ? event.getOrganizer() : request.organizer().trim(),
                status
        );
        if (request.startsAt() != null) {
            event.setStartsAt(request.startsAt());
        }
        try {
            return EventResponse.from(eventRepository.saveAndFlush(event));
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ConflictException("Событие уже было изменено другим процессом. Обновите страницу и повторите попытку.");
        }
    }

    @Transactional
    public void delete(Long id) {
        Event event = findEvent(id);
        eventRepository.delete(event);
    }

    private Event findEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Событие с идентификатором " + id + " не найдено."));
    }
}
