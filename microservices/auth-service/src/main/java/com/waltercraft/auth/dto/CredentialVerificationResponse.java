package com.waltercraft.auth.dto;

import java.util.Set;

public record CredentialVerificationResponse(
        boolean authenticated,
        Long userId,
        String username,
        Set<String> roles
) {
}
