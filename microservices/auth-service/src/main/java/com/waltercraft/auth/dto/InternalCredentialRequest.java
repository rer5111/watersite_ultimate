package com.waltercraft.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InternalCredentialRequest(
        @NotBlank @Size(max = 64) String username,
        @NotBlank @Size(max = 72) String password
) {
}
