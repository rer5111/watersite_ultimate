package com.waltercraft.auth.dto;

import java.util.Set;

public record AuthenticatedUser(Long id, String username, Set<String> roles) {
    public AuthenticatedUser {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }
}
