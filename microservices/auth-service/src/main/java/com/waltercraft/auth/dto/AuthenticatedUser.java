package com.waltercraft.auth.dto;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedUser(Long id, String username, UUID uuid, Set<String> roles) {
    public AuthenticatedUser {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    public AuthenticatedUser(Long id, String username, Set<String> roles) {
        this(id, username, null, roles);
    }
}
