package org.example.userservice.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public record InternalChangeEmailRequest(
        long userId,
        @NotBlank @Email String newEmail
) {
}
