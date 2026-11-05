package org.example.userservice.dto;

import org.example.userservice.model.RoleEnum;
import org.example.userservice.model.UserStatus;

import javax.validation.constraints.NotNull;

public record AdminUserUpdateRequest(
        @NotNull(message = "Role is required") RoleEnum role,
        @NotNull(message = "Status is required") UserStatus status
) {
}
