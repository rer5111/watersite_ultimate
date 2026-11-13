package com.waltercraft.event.repository;

import com.waltercraft.event.entity.Event;
import com.waltercraft.event.entity.EventStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @EntityGraph(attributePaths = {"participants"})
    Optional<Event> findWithParticipantsById(Long id);

    @EntityGraph(attributePaths = {"participants"})
    Optional<Event> findWithParticipantsByCode(String code);

    Optional<Event> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("SELECT DISTINCT e.seasonNumber FROM Event e WHERE e.active = true ORDER BY e.seasonNumber ASC")
    List<Integer> findDistinctActiveSeasons();

    @Query("SELECT DISTINCT e.organizer FROM Event e WHERE e.active = true AND e.organizer <> '' ORDER BY e.organizer ASC")
    List<String> findDistinctActiveOrganizers();

    @Query("SELECT DISTINCT e.status FROM Event e WHERE e.active = true ORDER BY e.status ASC")
    List<EventStatus> findDistinctActiveStatuses();

    @Query("SELECT MIN(e.startDate) FROM Event e WHERE e.active = true")
    LocalDate findMinStartDate();

    @Query("SELECT MAX(e.endDate) FROM Event e WHERE e.active = true")
    LocalDate findMaxEndDate();
}
