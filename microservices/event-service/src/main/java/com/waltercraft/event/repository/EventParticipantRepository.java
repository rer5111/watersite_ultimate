package com.waltercraft.event.repository;

import com.waltercraft.event.entity.EventParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventParticipantRepository extends JpaRepository<EventParticipant, Long> {

    List<EventParticipant> findByEventIdOrderByIdAsc(Long eventId);

    Optional<EventParticipant> findByEventIdAndUsernameIgnoreCase(Long eventId, String username);

    boolean existsByEventIdAndUsernameIgnoreCase(Long eventId, String username);
}
