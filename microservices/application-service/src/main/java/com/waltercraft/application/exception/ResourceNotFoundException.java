package com.waltercraft.application.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
        super("Заявка не найдена");
    }
}
