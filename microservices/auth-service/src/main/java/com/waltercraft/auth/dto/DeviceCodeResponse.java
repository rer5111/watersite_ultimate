package com.waltercraft.auth.dto;

public record DeviceCodeResponse(
        String deviceCode,
        String userCode,
        String verificationUri,
        int expiresIn,
        int interval
) {
}
