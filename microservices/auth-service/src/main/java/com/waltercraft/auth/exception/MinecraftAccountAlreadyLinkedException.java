package com.waltercraft.auth.exception;

public class MinecraftAccountAlreadyLinkedException extends RuntimeException {
    public MinecraftAccountAlreadyLinkedException() {
        super("Данный аккаунт Minecraft уже привязан к другому пользователю");
    }
}
