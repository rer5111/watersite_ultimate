package com.waltercraft.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Код подтверждения обязателен")
        @Size(min = 6, max = 6, message = "Код подтверждения должен состоять из 6 цифр")
        String code,

        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 8, message = "Пароль должен содержать не менее 8 символов")
        String newPassword
) {
}
