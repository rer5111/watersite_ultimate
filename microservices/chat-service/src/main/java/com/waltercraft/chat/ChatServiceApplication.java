package com.waltercraft.chat;

import com.waltercraft.chat.config.ChatJwtProperties;
import com.waltercraft.chat.config.DiscordBotProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ChatJwtProperties.class, DiscordBotProperties.class})
public class ChatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatServiceApplication.class, args);
    }
}
