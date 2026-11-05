package com.waltercraft.notification.exception;

public class RateLimitExceededException extends RuntimeException {
    private final int retryAfterSeconds;

    public RateLimitExceededException(int retryAfterSeconds) {
        super("Запрос нового кода возможен через " + retryAfterSeconds + " сек.");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
