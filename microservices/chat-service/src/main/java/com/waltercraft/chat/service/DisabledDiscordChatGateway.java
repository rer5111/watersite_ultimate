package com.waltercraft.chat.service;

import com.waltercraft.chat.exception.DiscordUnavailableException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "discord.bot.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledDiscordChatGateway implements DiscordChatGateway {

    @Override
    public boolean isConfigured() {
        return false;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void send(String username, String message) {
        throw new DiscordUnavailableException();
    }
}
