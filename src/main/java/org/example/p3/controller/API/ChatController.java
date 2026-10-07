package org.example.p3.controller.API;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/minecraft-chat")
public class ChatController extends ListenerAdapter {

    @Autowired
    private JDA jda;

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // <-- Injected socket engine

    @Value("${discord.chat.channel.id}")
    private String chatChannelId;

    private static final Pattern CHAT_PATTERN = Pattern.compile("^<([^>]+)>\\s(.*)$");

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getChannel().getId().equals(chatChannelId)) {

            boolean isOurMinecraftBot = event.getAuthor().getId().equals("1042883408596062298");
            boolean isAHumanUser = !event.getAuthor().isBot();

            if (isAHumanUser || isOurMinecraftBot) {
                String sender = event.getAuthor().getName();
                String messageText = event.getMessage().getContentDisplay();

                if (isOurMinecraftBot) {
                    // Check if the message matches the player chat pattern <PlayerName> text
                    Matcher matcher = CHAT_PATTERN.matcher(messageText);

                    if (matcher.matches()) {
                        // Split them cleanly!
                        sender = matcher.group(1);     // Extracts the name inside the brackets (e.g., rer_5111)
                        messageText = matcher.group(2); // Extracts the actual text message
                    } else {
                        // If it doesn't match brackets, it's a server event (e.g., **rer_5111 joined the game**)
                        sender = "Minecraft Server";
                    }
                }

                Map<String, String> chatPayload = Map.of(
                        "user", sender,
                        "message", messageText
                );

                messagingTemplate.convertAndSend("/topic/chat", chatPayload);
            }
        }
    }

    @PostMapping("/send")
    @ResponseBody
    public ResponseEntity<String> sendWebMessageToGame(@RequestParam String message, Principal principal) {

        String username = "Anonymous";

        if (principal != null) {
            username = principal.getName();
        }

        String formattedMessage = String.format("[Web] %s: %s", username, message);

        jda.getTextChannelById(chatChannelId)
                .sendMessage(formattedMessage)
                .queue();

        return ResponseEntity.ok("Message sent successfully");
    }

    @Autowired
    public void registerListener(JDA jda) {
        jda.addEventListener(this);
    }
}