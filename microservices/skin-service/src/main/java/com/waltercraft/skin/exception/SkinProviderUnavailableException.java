package com.waltercraft.skin.exception;

public class SkinProviderUnavailableException extends RuntimeException {

    public SkinProviderUnavailableException(Throwable cause) {
        super("Skin provider is temporarily unavailable", cause);
    }

    public SkinProviderUnavailableException(String message) {
        super(message);
    }
}
