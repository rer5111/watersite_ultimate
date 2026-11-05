package com.waltercraft.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterVerifyCodeRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 3, max = 32) String username,
        @NotBlank @Size(min = 6, max = 72) String password,
        @NotBlank @Pattern(regexp = "^\\d{6}$", message = "Код должен содержать 6 цифр") String code
) {
}
