package com.waltercraft.auth.dto;

public record InternalVerifyCodeRequest(String email, String code, String purpose) {
}
