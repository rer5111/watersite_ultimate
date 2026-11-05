package com.waltercraft.auth.dto;

public record InternalChangePasswordRequest(
        long userId,
        String newPassword
) {
}
