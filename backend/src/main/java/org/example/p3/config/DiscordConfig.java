package org.example.p3.config;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscordConfig {
    @Value("${discord.bot.token}")
    private String BOT_TOKEN;

    @Bean
    public JDA jdaInstance() throws InterruptedException {
        return JDABuilder.createDefault(BOT_TOKEN)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
                .build()
                .awaitReady();
    }
}
