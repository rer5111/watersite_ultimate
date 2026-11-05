package com.waltercraft.auth.dto;

public record InternalVerifyCodeResponse(boolean verified, String message) {
}
