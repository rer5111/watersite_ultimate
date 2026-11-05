package com.waltercraft.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record DeviceLoginRequest(
        @NotBlank(message = "deviceCode обязателен")
        String deviceCode
) {
}
