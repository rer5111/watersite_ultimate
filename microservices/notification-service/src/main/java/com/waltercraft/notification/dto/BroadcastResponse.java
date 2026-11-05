package com.waltercraft.notification.dto;

import com.waltercraft.notification.entity.Broadcast;
import com.waltercraft.notification.entity.BroadcastAudience;
import com.waltercraft.notification.entity.BroadcastCategory;
import com.waltercraft.notification.entity.BroadcastStatus;

import java.time.Instant;

public record BroadcastResponse(
        Long id,
        String subject,
        BroadcastCategory category,
        BroadcastAudience audience,
        String message,
        Instant sentAt,
        int recipients,
        BroadcastStatus status
) {
    public static BroadcastResponse from(Broadcast broadcast) {
        return new BroadcastResponse(
                broadcast.getId(),
                broadcast.getSubject(),
                broadcast.getCategory(),
                broadcast.getAudience(),
                broadcast.getMessage(),
                broadcast.getSentAt(),
                broadcast.getRecipients(),
                broadcast.getStatus()
        );
    }
}
