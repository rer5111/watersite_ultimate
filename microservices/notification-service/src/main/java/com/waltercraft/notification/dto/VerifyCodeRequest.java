package com.waltercraft.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyCodeRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Pattern(regexp = "^\\d{6}$", message = "Код должен состоять из 6 цифр") String code,
        @NotBlank @Size(max = 32) String purpose
) {
}
