package com.waltercraft.chat.controller;

import com.waltercraft.chat.dto.ChatMessageRequest;
import com.waltercraft.chat.dto.ChatMessageResponse;
import com.waltercraft.chat.dto.ChatStatusResponse;
import com.waltercraft.chat.dto.ChatSubmissionResponse;
import com.waltercraft.chat.service.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/messages")
    public List<ChatMessageResponse> messages(
            @RequestParam(defaultValue = "0") @PositiveOrZero long afterId
    ) {
        return chatService.messagesAfter(afterId);
    }

    @GetMapping("/status")
    public ChatStatusResponse status() {
        return chatService.status();
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatSubmissionResponse> send(
            @Valid @RequestBody ChatMessageRequest request,
            Authentication authentication
    ) {
        chatService.send(authentication.getName(), request.message().strip());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new ChatSubmissionResponse("accepted"));
    }
}
