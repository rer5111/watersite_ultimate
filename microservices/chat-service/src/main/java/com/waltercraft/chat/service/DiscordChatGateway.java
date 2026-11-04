package com.waltercraft.chat.service;

public interface DiscordChatGateway {

    boolean isConfigured();

    boolean isConnected();

    void send(String username, String message);
}
