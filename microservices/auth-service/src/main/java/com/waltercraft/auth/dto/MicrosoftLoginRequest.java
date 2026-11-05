package com.waltercraft.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record MicrosoftLoginRequest(
        @NotBlank(message = "Код авторизации обязателен")
        String code,
        String redirectUri
) {
}
