package com.waltercraft.chat.service;

import com.waltercraft.chat.config.DiscordBotProperties;
import com.waltercraft.chat.dto.ChatMessageResponse.Origin;
import com.waltercraft.chat.exception.DiscordUnavailableException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(name = "discord.bot.enabled", havingValue = "true")
public class JdaDiscordChatGateway extends ListenerAdapter implements DiscordChatGateway {

    private static final Logger log = LoggerFactory.getLogger(JdaDiscordChatGateway.class);
    private static final Pattern MINECRAFT_CHAT_PATTERN = Pattern.compile("^<([^>]+)>\\s(.*)$");
    private static final String WEB_MESSAGE_PREFIX = "[Web] ";

    private final DiscordBotProperties properties;
    private final ChatHistory history;
    private final AtomicBoolean connected = new AtomicBoolean();
    private JDA jda;

    public JdaDiscordChatGateway(DiscordBotProperties properties, ChatHistory history) {
        this.properties = properties;
        this.history = history;
    }

    @PostConstruct
    void connect() {
        if (properties.token() == null || properties.token().isBlank()
                || properties.channelId() == null || properties.channelId().isBlank()) {
            throw new IllegalStateException("Discord bot token and chat channel ID are required when the bot is enabled");
        }
        jda = JDABuilder.createDefault(properties.token())
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(this)
                .build();
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public boolean isConnected() {
        return connected.get();
    }

    @Override
    public void send(String username, String message) {
        if (!isConnected()) {
            throw new DiscordUnavailableException();
        }
        TextChannel channel = jda.getTextChannelById(properties.channelId());
        if (channel == null) {
            throw new DiscordUnavailableException();
        }
        channel.sendMessage(WEB_MESSAGE_PREFIX + username + ": " + message).queue(
                ignored -> history.publish(username, message, Origin.WEB),
                exception -> log.warn("Discord rejected a chat message", exception));
    }

    @Override
    public void onReady(ReadyEvent event) {
        connected.set(true);
        log.info("Discord chat bridge connected");
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        connected.set(false);
        log.info("Discord chat bridge disconnected");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getChannel().getId().equals(properties.channelId())) {
            return;
        }

        boolean minecraftBot = event.getAuthor().getId().equals(properties.minecraftBotUserId());
        if (event.getAuthor().isBot() && !minecraftBot) {
            return;
        }

        String username = event.getAuthor().getName();
        String message = event.getMessage().getContentDisplay();
        boolean ownWebRelay = message.startsWith(WEB_MESSAGE_PREFIX)
                && event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId());
        if (ownWebRelay) {
            return;
        }
        Origin origin = Origin.DISCORD;
        if (minecraftBot) {
            Matcher matcher = MINECRAFT_CHAT_PATTERN.matcher(message);
            if (matcher.matches()) {
                username = matcher.group(1);
                message = matcher.group(2);
            } else {
                username = "Minecraft Server";
            }
            origin = Origin.MINECRAFT;
        }
        if (!message.isBlank()) {
            history.publish(username, message, origin);
        }
    }

    @PreDestroy
    void shutdown() {
        if (jda != null) {
            jda.shutdownNow();
        }
    }
}
