package com.waltercraft.auth.exception;

public class MinecraftProviderUnavailableException extends RuntimeException {

    public MinecraftProviderUnavailableException(Throwable cause) {
        super("Minecraft identity providers are temporarily unavailable", cause);
    }
}
