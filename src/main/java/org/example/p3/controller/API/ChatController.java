package org.example.p3.controller.API;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.security.Principal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/api/minecraft-chat")
@RequiredArgsConstructor
public class ChatController extends ListenerAdapter {

    private final JDA jda;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${discord.chat.channel.id}")
    private String chatChannelId;

    private static final String BOT_USER_ID = "1042883408596062298";
    private static final Pattern CHAT_PATTERN = Pattern.compile("^<([^>]+)>\\s(.*)$");

    @PostConstruct
    public void registerListener() {
        jda.addEventListener(this);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getChannel().getId().equals(chatChannelId)) {
            return;
        }

        boolean isOurMinecraftBot = event.getAuthor().getId().equals(BOT_USER_ID);
        boolean isAHumanUser = !event.getAuthor().isBot();

        if (!isAHumanUser && !isOurMinecraftBot) {
            return;
        }

        Map<String, String> chatPayload = getChatPayload(event, isOurMinecraftBot);

        messagingTemplate.convertAndSend("/topic/chat", chatPayload);
    }

    private static Map<String, String> getChatPayload(MessageReceivedEvent event, boolean isOurMinecraftBot) {
        String sender = event.getAuthor().getName();
        String messageText = event.getMessage().getContentDisplay();

        if (isOurMinecraftBot) {
            Matcher matcher = CHAT_PATTERN.matcher(messageText);
            if (matcher.matches()) {
                sender = matcher.group(1);
                messageText = matcher.group(2);
            } else {
                sender = "Minecraft Server";
            }
        }

        return Map.of(
                "user", sender,
                "message", messageText
        );
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendWebMessageToGame(@RequestParam String message, Principal principal) {
        String username = (principal != null) ? principal.getName() : "Anonymous";
        String formattedMessage = String.format("[Web] %s: %s", username, message);

        TextChannel channel = jda.getTextChannelById(chatChannelId);
        if (channel == null) {
            log.error("Configured Discord channel ID '{}' was not found by JDA", chatChannelId);
            return ResponseEntity.badRequest().body("Discord channel not found");
        }

        channel.sendMessage(formattedMessage).queue();
        return ResponseEntity.ok("Message sent successfully");
    }
}