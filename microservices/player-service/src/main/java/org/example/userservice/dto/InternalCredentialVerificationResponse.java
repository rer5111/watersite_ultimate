package org.example.userservice.dto;

import java.util.Set;
import java.util.UUID;

public record InternalCredentialVerificationResponse(
        boolean authenticated,
        Long userId,
        String username,
        Set<String> roles,
        UUID uuid
) {
    public InternalCredentialVerificationResponse(boolean authenticated, Long userId, String username, Set<String> roles) {
        this(authenticated, userId, username, roles, null);
    }

    public static InternalCredentialVerificationResponse rejected() {
        return new InternalCredentialVerificationResponse(false, null, null, Set.of(), null);
    }
}
