package com.waltercraft.notification.dto;

public record VerifyCodeResponse(
        boolean verified,
        String message
) {
    public static VerifyCodeResponse success() {
        return new VerifyCodeResponse(true, "Код успешно подтвержден");
    }

    public static VerifyCodeResponse failure(String message) {
        return new VerifyCodeResponse(false, message);
    }
}
