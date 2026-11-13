package com.waltercraft.event.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EventStatus {
    PLANNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    @JsonCreator
    public static EventStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (EventStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Неизвестный статус ивента: " + value);
    }
}
