package org.example.userservice.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public record InternalCredentialRequest(
        @NotBlank @Size(max = 64) String username,
        @NotBlank @Size(max = 72) String password
) {
}
