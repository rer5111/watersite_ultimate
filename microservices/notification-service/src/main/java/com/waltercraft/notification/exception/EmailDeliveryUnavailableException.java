package com.waltercraft.notification.exception;

public class EmailDeliveryUnavailableException extends RuntimeException {

    public EmailDeliveryUnavailableException(Throwable cause) {
        super("Email delivery is temporarily unavailable", cause);
    }

    public EmailDeliveryUnavailableException() {
        super("Email delivery is temporarily unavailable");
    }
}
