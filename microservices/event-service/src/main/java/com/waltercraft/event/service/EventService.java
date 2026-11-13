package com.waltercraft.event.service;

import com.waltercraft.event.dto.AddParticipantRequest;
import com.waltercraft.event.dto.EventCreateRequest;
import com.waltercraft.event.dto.EventDetailResponse;
import com.waltercraft.event.dto.EventFiltersResponse;
import com.waltercraft.event.dto.EventParticipantResponse;
import com.waltercraft.event.dto.EventSummaryResponse;
import com.waltercraft.event.dto.EventUpdateRequest;
import com.waltercraft.event.entity.Event;
import com.waltercraft.event.entity.EventParticipant;
import com.waltercraft.event.entity.EventStatus;
import com.waltercraft.event.exception.ConflictException;
import com.waltercraft.event.exception.ResourceNotFoundException;
import com.waltercraft.event.repository.EventParticipantRepository;
import com.waltercraft.event.repository.EventRepository;
import com.waltercraft.event.repository.EventSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final EventParticipantRepository participantRepository;

    public EventService(EventRepository eventRepository, EventParticipantRepository participantRepository) {
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
    }

    @Transactional(readOnly = true)
    public Page<EventSummaryResponse> getEvents(
            List<Integer> seasons,
            List<String> organizers,
            List<EventStatus> statuses,
            LocalDate dateFrom,
            LocalDate dateTo,
            String search,
            Pageable pageable
    ) {
        Specification<Event> spec = EventSpecification.filter(
                seasons, organizers, statuses, dateFrom, dateTo, search, true
        );

        // Default sort: displayOrder ASC, id DESC
        Pageable effectivePageable = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "displayOrder").and(Sort.by(Sort.Direction.DESC, "id")));

        return eventRepository.findAll(spec, effectivePageable).map(EventSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<EventSummaryResponse> getAllEventsAdmin(
            List<Integer> seasons,
            String organizer,
            List<EventStatus> statuses,
            LocalDate dateFrom,
            LocalDate dateTo,
            String search,
            Pageable pageable
    ) {
        List<String> organizers = organizer != null && !organizer.isBlank()
                ? List.of(organizer)
                : null;
        Specification<Event> spec = EventSpecification.filter(
                seasons, organizers, statuses, dateFrom, dateTo, search, false
        );

        Pageable effectivePageable = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "displayOrder").and(Sort.by(Sort.Direction.DESC, "id")));

        return eventRepository.findAll(spec, effectivePageable).map(EventSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public EventDetailResponse getEventByIdOrCode(String identifier) {
        Event event = findEventByIdentifier(identifier);
        return EventDetailResponse.from(event);
    }

    @Transactional(readOnly = true)
    public EventFiltersResponse getFilters() {
        List<Integer> seasons = eventRepository.findDistinctActiveSeasons();
        List<String> organizers = eventRepository.findDistinctActiveOrganizers();
        List<EventStatus> statuses = eventRepository.findDistinctActiveStatuses();
        LocalDate minDate = eventRepository.findMinStartDate();
        LocalDate maxDate = eventRepository.findMaxEndDate();

        return new EventFiltersResponse(seasons, organizers, statuses, minDate, maxDate);
    }

    @Transactional
    public EventDetailResponse participate(Long eventId, Long userId, String username, String notes) {
        Event event = eventRepository.findWithParticipantsById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Ивент не найден: " + eventId));

        if (!event.getActive()) {
            throw new ConflictException("Нельзя записаться на неактивный ивент");
        }

        if (participantRepository.existsByEventIdAndUsernameIgnoreCase(eventId, username)) {
            throw new ConflictException("Вы уже записаны на этот ивент");
        }

        EventParticipant participant = new EventParticipant(event, userId, username, "PARTICIPANT", notes);
        event.addParticipant(participant);
        participantRepository.save(participant);

        log.info("User {} registered as participant for event {}", username, eventId);
        return EventDetailResponse.from(event);
    }

    @Transactional
    public EventDetailResponse createEvent(EventCreateRequest request) {
        if (eventRepository.existsByCode(request.code())) {
            throw new ConflictException("Ивент с таким кодом уже существует: " + request.code());
        }

        Event event = new Event();
        event.setCode(request.code().trim().toLowerCase());
        event.setTitle(request.title().trim());
        event.setDescription(request.description().trim());
        event.setShortDescription(request.shortDescription() != null ? request.shortDescription().trim() : "");
        event.setImage(request.image() != null ? request.image().trim() : "");
        event.setSeasonNumber(request.seasonNumber());
        event.setOrganizer(request.organizer().trim());
        event.setStatus(request.status());
        event.setStartDate(request.startDate());
        event.setEndDate(request.endDate());
        event.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);
        event.setActive(request.active() != null ? request.active() : true);

        Event saved = eventRepository.save(event);
        log.info("Created event id={} code={}", saved.getId(), saved.getCode());
        return EventDetailResponse.from(saved);
    }

    @Transactional
    public EventDetailResponse updateEvent(Long id, EventUpdateRequest request) {
        Event event = eventRepository.findWithParticipantsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ивент не найден: " + id));

        if (request.title() != null) {
            event.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            event.setDescription(request.description().trim());
        }
        if (request.shortDescription() != null) {
            event.setShortDescription(request.shortDescription().trim());
        }
        if (request.image() != null) {
            event.setImage(request.image().trim());
        }
        if (request.seasonNumber() != null) {
            event.setSeasonNumber(request.seasonNumber());
        }
        if (request.organizer() != null) {
            event.setOrganizer(request.organizer().trim());
        }
        if (request.status() != null) {
            event.setStatus(request.status());
        }
        if (request.startDate() != null) {
            event.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            event.setEndDate(request.endDate());
        }
        if (request.displayOrder() != null) {
            event.setDisplayOrder(request.displayOrder());
        }
        if (request.active() != null) {
            event.setActive(request.active());
        }

        Event updated = eventRepository.save(event);
        log.info("Updated event id={}", updated.getId());
        return EventDetailResponse.from(updated);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ивент не найден: " + id));
        eventRepository.delete(event);
        log.info("Deleted event id={}", id);
    }

    @Transactional
    public EventParticipantResponse addParticipant(Long eventId, AddParticipantRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Ивент не найден: " + eventId));

        if (participantRepository.existsByEventIdAndUsernameIgnoreCase(eventId, request.username())) {
            throw new ConflictException("Участник с ником " + request.username() + " уже добавлен");
        }

        String role = request.role() != null && !request.role().isBlank() ? request.role().trim() : "PARTICIPANT";
        EventParticipant participant = new EventParticipant(event, null, request.username().trim(), role, request.notes());
        event.addParticipant(participant);
        EventParticipant saved = participantRepository.save(participant);

        return EventParticipantResponse.from(saved);
    }

    @Transactional
    public void removeParticipant(Long eventId, Long participantId) {
        EventParticipant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Участник не найден: " + participantId));

        if (!participant.getEvent().getId().equals(eventId)) {
            throw new ConflictException("Участник не принадлежит данному ивенту");
        }

        participant.getEvent().removeParticipant(participant);
        participantRepository.delete(participant);
        log.info("Removed participant {} from event {}", participantId, eventId);
    }

    private Event findEventByIdentifier(String identifier) {
        try {
            Long id = Long.parseLong(identifier);
            return eventRepository.findWithParticipantsById(id)
                    .orElseGet(() -> eventRepository.findWithParticipantsByCode(identifier)
                            .orElseThrow(() -> new ResourceNotFoundException("Ивент не найден: " + identifier)));
        } catch (NumberFormatException e) {
            return eventRepository.findWithParticipantsByCode(identifier)
                    .orElseThrow(() -> new ResourceNotFoundException("Ивент не найден: " + identifier));
        }
    }
}
