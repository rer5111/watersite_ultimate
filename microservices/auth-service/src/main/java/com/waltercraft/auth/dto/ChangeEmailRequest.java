package com.waltercraft.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeEmailRequest(
        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Некорректный формат email")
        @Size(max = 255)
        String newEmail,

        @NotBlank(message = "Код подтверждения обязателен")
        @Size(min = 6, max = 6, message = "Код подтверждения должен состоять из 6 цифр")
        String code
) {
}
