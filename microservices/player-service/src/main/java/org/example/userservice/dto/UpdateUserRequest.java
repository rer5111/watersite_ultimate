package org.example.userservice.dto;

import org.example.userservice.model.RoleEnum;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

public record UpdateUserRequest(
        long id,
        @NotBlank String name,
        @NotBlank String password,
        @NotNull Set<RoleEnum> roles,
        long accountId
) {}
