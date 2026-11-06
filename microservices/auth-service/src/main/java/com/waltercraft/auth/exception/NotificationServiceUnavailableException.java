package com.waltercraft.auth.exception;

public class NotificationServiceUnavailableException extends RuntimeException {

    public NotificationServiceUnavailableException(Throwable cause) {
        super("Notification service is temporarily unavailable", cause);
    }
}
