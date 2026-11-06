package com.waltercraft.chat.service;

import com.waltercraft.chat.config.DiscordBotProperties;
import com.waltercraft.chat.dto.ChatMessageResponse.Origin;
import com.waltercraft.chat.exception.DiscordUnavailableException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JdaDiscordChatGatewayTest {

    private static final String BOT_ID = "1042883408596062298";
    private static final String CHANNEL_ID = "channel";

    private final ChatHistory history = new ChatHistory();
    private final CircuitBreakerRegistry circuitBreakers = CircuitBreakerRegistry.of(
            CircuitBreakerConfig.custom()
                    .slidingWindowSize(10)
                    .minimumNumberOfCalls(10)
                    .failureRateThreshold(50)
                    .waitDurationInOpenState(java.time.Duration.ofSeconds(10))
                    .build());
    private final JdaDiscordChatGateway gateway = new JdaDiscordChatGateway(
            new DiscordBotProperties(true, "token", CHANNEL_ID, BOT_ID), history, circuitBreakers);

    @Test
    void ignoresTheWebRelayEchoFromItsOwnBot() {
        gateway.onMessageReceived(event(BOT_ID, "[Web] player: hello"));

        assertTrue(history.after(0).isEmpty());
    }

    @Test
    void publishesMinecraftChatRelayedByTheBot() {
        gateway.onMessageReceived(event(BOT_ID, "<Steve> hello"));

        var messages = history.after(0);
        assertEquals(1, messages.size());
        assertEquals("Steve", messages.getFirst().user());
        assertEquals(Origin.MINECRAFT, messages.getFirst().origin());
    }

    @Test
    void opensCircuitAndReturnsControlledFallbackWhenDiscordStaysDisconnected() {
        for (int attempt = 0; attempt < 10; attempt++) {
            assertThrows(DiscordUnavailableException.class, () -> gateway.send("player", "hello"));
        }

        assertEquals(io.github.resilience4j.circuitbreaker.CircuitBreaker.State.OPEN,
                circuitBreakers.circuitBreaker("discord").getState());
        assertThrows(DiscordUnavailableException.class, () -> gateway.send("player", "hello"));
    }

    private MessageReceivedEvent event(String authorId, String content) {
        JDA jda = mock(JDA.class);
        SelfUser selfUser = mock(SelfUser.class);
        when(selfUser.getId()).thenReturn(BOT_ID);
        when(jda.getSelfUser()).thenReturn(selfUser);

        MessageChannelUnion channel = mock(MessageChannelUnion.class);
        when(channel.getId()).thenReturn(CHANNEL_ID);

        User author = mock(User.class);
        when(author.getId()).thenReturn(authorId);
        when(author.isBot()).thenReturn(true);
        when(author.getName()).thenReturn("WalterWhite");

        Message message = mock(Message.class);
        when(message.getContentDisplay()).thenReturn(content);
        when(message.getAuthor()).thenReturn(author);
        when(message.getChannel()).thenReturn(channel);

        return new MessageReceivedEvent(jda, 0, message);
    }
}
