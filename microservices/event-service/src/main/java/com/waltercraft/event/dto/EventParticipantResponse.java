package com.waltercraft.event.dto;

import com.waltercraft.event.entity.EventParticipant;

import java.time.Instant;

public record EventParticipantResponse(
        Long id,
        Long userId,
        String username,
        String role,
        String notes,
        Instant createdAt
) {
    public static EventParticipantResponse from(EventParticipant participant) {
        return new EventParticipantResponse(
                participant.getId(),
                participant.getUserId(),
                participant.getUsername(),
                participant.getRole(),
                participant.getNotes(),
                participant.getCreatedAt()
        );
    }
}
