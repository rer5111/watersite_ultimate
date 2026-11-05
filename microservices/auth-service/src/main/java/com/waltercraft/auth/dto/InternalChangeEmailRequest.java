package com.waltercraft.auth.dto;

public record InternalChangeEmailRequest(
        long userId,
        String newEmail
) {
}
