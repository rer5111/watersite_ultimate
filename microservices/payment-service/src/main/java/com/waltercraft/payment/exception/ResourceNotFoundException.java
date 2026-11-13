package com.waltercraft.payment.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
        super("Платёж не найден");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
