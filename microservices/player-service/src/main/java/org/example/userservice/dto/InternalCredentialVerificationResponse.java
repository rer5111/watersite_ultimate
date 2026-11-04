package org.example.userservice.dto;

import java.util.Set;

public record InternalCredentialVerificationResponse(
        boolean authenticated,
        Long userId,
        String username,
        Set<String> roles
) {
    public static InternalCredentialVerificationResponse rejected() {
        return new InternalCredentialVerificationResponse(false, null, null, Set.of());
    }
}
