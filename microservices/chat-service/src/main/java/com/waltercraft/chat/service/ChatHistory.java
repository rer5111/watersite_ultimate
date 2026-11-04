package com.waltercraft.chat.service;

import com.waltercraft.chat.dto.ChatMessageResponse;
import com.waltercraft.chat.dto.ChatMessageResponse.Origin;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ChatHistory {

    private static final int MAX_MESSAGES = 100;

    private final AtomicLong sequence = new AtomicLong();
    private final ArrayDeque<ChatMessageResponse> messages = new ArrayDeque<>();

    public synchronized ChatMessageResponse publish(String user, String message, Origin origin) {
        ChatMessageResponse response = new ChatMessageResponse(
                sequence.incrementAndGet(), user, message, origin, Instant.now());
        messages.addLast(response);
        while (messages.size() > MAX_MESSAGES) {
            messages.removeFirst();
        }
        return response;
    }

    public synchronized List<ChatMessageResponse> after(long messageId) {
        return messages.stream()
                .filter(message -> message.id() > messageId)
                .toList();
    }
}
