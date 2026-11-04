package com.waltercraft.chat.service;

import com.waltercraft.chat.dto.ChatMessageResponse;
import com.waltercraft.chat.dto.ChatMessageResponse.Origin;
import com.waltercraft.chat.dto.ChatStatusResponse;
import com.waltercraft.chat.exception.DiscordUnavailableException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final ChatHistory history;
    private final DiscordChatGateway discordChatGateway;

    public ChatService(ChatHistory history, DiscordChatGateway discordChatGateway) {
        this.history = history;
        this.discordChatGateway = discordChatGateway;
    }

    public List<ChatMessageResponse> messagesAfter(long messageId) {
        return history.after(messageId);
    }

    public ChatStatusResponse status() {
        return new ChatStatusResponse(discordChatGateway.isConfigured(), discordChatGateway.isConnected());
    }

    public void send(String username, String message) {
        if (!discordChatGateway.isConfigured() || !discordChatGateway.isConnected()) {
            throw new DiscordUnavailableException();
        }
        discordChatGateway.send(username, message);
    }

    public ChatMessageResponse publishDiscordMessage(String username, String message) {
        return history.publish(username, message, Origin.DISCORD);
    }
}
