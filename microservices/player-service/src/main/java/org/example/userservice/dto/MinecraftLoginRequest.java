package org.example.userservice.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

public record MinecraftLoginRequest(
        @NotNull UUID uuid,
        @NotBlank String username
) {
}
