package org.example.userservice.dto;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record UserProfileResponse(
        long id,
        String username,
        String email,
        String minecraftNickname,
        UUID minecraftUuid,
        LocalDate joinDate,
        Set<String> roles
) {
}
