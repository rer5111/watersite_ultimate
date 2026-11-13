package com.waltercraft.notification.dto;

import com.waltercraft.notification.entity.BroadcastCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserNotificationRequest(
        @NotNull Long userId,
        @NotBlank @Size(max = 120) String subject,
        @NotNull BroadcastCategory category,
        @NotBlank @Size(max = 2000) String message
) {
}
