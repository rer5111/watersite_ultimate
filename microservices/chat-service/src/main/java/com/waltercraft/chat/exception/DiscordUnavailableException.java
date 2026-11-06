package com.waltercraft.chat.exception;

public class DiscordUnavailableException extends RuntimeException {

    public DiscordUnavailableException() {
        super("Discord-бот не настроен или сейчас не подключён");
    }

    public DiscordUnavailableException(Throwable cause) {
        super("Discord-бот не настроен или сейчас не подключён", cause);
    }
}
