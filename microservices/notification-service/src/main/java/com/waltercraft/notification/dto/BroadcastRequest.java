package com.waltercraft.notification.dto;

import com.waltercraft.notification.entity.BroadcastAudience;
import com.waltercraft.notification.entity.BroadcastCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record BroadcastRequest(
        @NotBlank @Size(max = 120) String subject,
        @NotNull BroadcastCategory category,
        @NotNull BroadcastAudience audience,
        @NotBlank @Size(min = 10, max = 2000) String message,
        @NotNull @PositiveOrZero Integer recipients
) {
}
