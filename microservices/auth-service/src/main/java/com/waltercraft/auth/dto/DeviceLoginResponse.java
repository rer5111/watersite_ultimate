package com.waltercraft.auth.dto;

public record DeviceLoginResponse(
        String status,
        TokenResponse tokens
) {
}
