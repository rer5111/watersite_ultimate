package com.waltercraft.auth.dto;

import java.util.Set;
import java.util.UUID;

public record CredentialVerificationResponse(
        boolean authenticated,
        Long userId,
        String username,
        Set<String> roles,
        UUID uuid
) {
    public CredentialVerificationResponse(boolean authenticated, Long userId, String username, Set<String> roles) {
        this(authenticated, userId, username, roles, null);
    }
}
