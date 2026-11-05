package com.waltercraft.notification.dto;

import com.waltercraft.notification.entity.Broadcast;
import com.waltercraft.notification.entity.BroadcastCategory;

import java.time.Instant;

public record UserNotificationResponse(
        Long id,
        String subject,
        BroadcastCategory category,
        String message,
        Instant sentAt,
        boolean read
) {
    public static UserNotificationResponse from(Broadcast broadcast, boolean read) {
        return new UserNotificationResponse(
                broadcast.getId(),
                broadcast.getSubject(),
                broadcast.getCategory(),
                broadcast.getMessage(),
                broadcast.getSentAt(),
                read
        );
    }
}
