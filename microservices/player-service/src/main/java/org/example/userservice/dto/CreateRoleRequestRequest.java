package org.example.userservice.dto;

import org.example.userservice.model.RoleEnum;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record CreateRoleRequestRequest(
        @NotNull(message = "Role is required")
        RoleEnum requestedRole,

        @NotBlank(message = "Message is required")
        @Size(min = 10, max = 2000, message = "Message must contain from 10 to 2000 characters")
        String note
) {
}
