package org.example.userservice.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public record InternalChangePasswordRequest(
        long userId,
        @NotBlank @Size(min = 8) String newPassword
) {
}
