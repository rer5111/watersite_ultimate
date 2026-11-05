package org.example.userservice.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public record RoleRejectRequest(
        @NotBlank(message = "Rejection reason is required")
        @Size(max = 1000)
        String reason
) {
}
