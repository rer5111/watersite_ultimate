package com.waltercraft.auth.exception;

public class MinecraftProfileNotFoundException extends RuntimeException {
    public MinecraftProfileNotFoundException() {
        super("Аккаунт Microsoft не содержит лицензионного профиля Minecraft Java Edition");
    }
}
