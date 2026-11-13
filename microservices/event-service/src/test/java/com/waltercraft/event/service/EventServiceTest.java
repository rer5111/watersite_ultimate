package com.waltercraft.event.service;

import com.waltercraft.event.dto.EventCreateRequest;
import com.waltercraft.event.dto.EventDetailResponse;
import com.waltercraft.event.dto.EventFiltersResponse;
import com.waltercraft.event.dto.EventSummaryResponse;
import com.waltercraft.event.dto.EventUpdateRequest;
import com.waltercraft.event.entity.Event;
import com.waltercraft.event.entity.EventParticipant;
import com.waltercraft.event.entity.EventStatus;
import com.waltercraft.event.exception.ConflictException;
import com.waltercraft.event.exception.ResourceNotFoundException;
import com.waltercraft.event.repository.EventParticipantRepository;
import com.waltercraft.event.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventParticipantRepository participantRepository;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository, participantRepository);
    }

    @Test
    @DisplayName("getEvents returns page of EventSummaryResponse")
    void getEvents_success() {
        Event event = createSampleEvent(1L, "event-1", "ивент 1", 1, "котярендж", EventStatus.COMPLETED);
        when(eventRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(event)));

        Page<EventSummaryResponse> result = eventService.getEvents(
                List.of(1), List.of("котярендж"), List.of(EventStatus.COMPLETED), null, null, null, PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("ивент 1");
        assertThat(result.getContent().get(0).seasonNumber()).isEqualTo(1);
        assertThat(result.getContent().get(0).seasonTitle()).isEqualTo("1 сезон");
        assertThat(result.getContent().get(0).organizer()).isEqualTo("котярендж");
    }

    @Test
    @DisplayName("getEventByIdOrCode returns details by code")
    void getEventByIdOrCode_byCode() {
        Event event = createSampleEvent(1L, "event-1", "ивент 1", 1, "котярендж", EventStatus.COMPLETED);
        EventParticipant participant = new EventParticipant(event, 10L, "Steve", "PARTICIPANT", null);
        event.addParticipant(participant);

        when(eventRepository.findWithParticipantsByCode("event-1")).thenReturn(Optional.of(event));

        EventDetailResponse detail = eventService.getEventByIdOrCode("event-1");

        assertThat(detail.title()).isEqualTo("ивент 1");
        assertThat(detail.participantsCount()).isEqualTo(1);
        assertThat(detail.participants().get(0).username()).isEqualTo("Steve");
    }

    @Test
    @DisplayName("getEventByIdOrCode throws when not found")
    void getEventByIdOrCode_notFound() {
        when(eventRepository.findWithParticipantsByCode("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventByIdOrCode("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getFilters returns available season and organizer choices")
    void getFilters_success() {
        when(eventRepository.findDistinctActiveSeasons()).thenReturn(List.of(1, 2, 3));
        when(eventRepository.findDistinctActiveOrganizers()).thenReturn(List.of("котярендж", "рер"));
        when(eventRepository.findDistinctActiveStatuses()).thenReturn(List.of(EventStatus.COMPLETED, EventStatus.PLANNED));
        when(eventRepository.findMinStartDate()).thenReturn(LocalDate.of(2020, 12, 12));
        when(eventRepository.findMaxEndDate()).thenReturn(LocalDate.of(2026, 12, 15));

        EventFiltersResponse filters = eventService.getFilters();

        assertThat(filters.seasons()).containsExactly(1, 2, 3);
        assertThat(filters.organizers()).containsExactly("котярендж", "рер");
        assertThat(filters.statuses()).containsExactly(EventStatus.COMPLETED, EventStatus.PLANNED);
    }

    @Test
    @DisplayName("participate adds user to event")
    void participate_success() {
        Event event = createSampleEvent(1L, "event-1", "ивент 1", 1, "котярендж", EventStatus.PLANNED);
        when(eventRepository.findWithParticipantsById(1L)).thenReturn(Optional.of(event));
        when(participantRepository.existsByEventIdAndUsernameIgnoreCase(1L, "Steve")).thenReturn(false);

        EventDetailResponse response = eventService.participate(1L, 5L, "Steve", "Готов побеждать");

        assertThat(response.participantsCount()).isEqualTo(1);
        assertThat(response.participants().get(0).username()).isEqualTo("Steve");
        verify(participantRepository).save(any(EventParticipant.class));
    }

    @Test
    @DisplayName("participate throws when already registered")
    void participate_alreadyRegistered() {
        Event event = createSampleEvent(1L, "event-1", "ивент 1", 1, "котярендж", EventStatus.PLANNED);
        when(eventRepository.findWithParticipantsById(1L)).thenReturn(Optional.of(event));
        when(participantRepository.existsByEventIdAndUsernameIgnoreCase(1L, "Steve")).thenReturn(true);

        assertThatThrownBy(() -> eventService.participate(1L, 5L, "Steve", null))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Вы уже записаны на этот ивент");
    }

    private Event createSampleEvent(Long id, String code, String title, int season, String organizer, EventStatus status) {
        Event event = new Event();
        event.setId(id);
        event.setCode(code);
        event.setTitle(title);
        event.setDescription("Описание " + title);
        event.setShortDescription("Краткое описание");
        event.setImage("/img.png");
        event.setSeasonNumber(season);
        event.setOrganizer(organizer);
        event.setStatus(status);
        event.setStartDate(LocalDate.of(2020, 12, 12));
        event.setEndDate(LocalDate.of(2020, 12, 22));
        event.setDisplayOrder(10);
        event.setActive(true);
        return event;
    }
}
