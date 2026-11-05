package com.waltercraft.auth.exception;

public class MinecraftOAuthException extends RuntimeException {
    public MinecraftOAuthException(String message) {
        super(message);
    }

    public MinecraftOAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
