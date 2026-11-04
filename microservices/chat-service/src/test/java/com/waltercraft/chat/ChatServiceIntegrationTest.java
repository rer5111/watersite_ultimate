package com.waltercraft.chat;

import com.waltercraft.chat.dto.ChatMessageResponse.Origin;
import com.waltercraft.chat.service.ChatHistory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "discord.bot.enabled=false"
})
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class ChatServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatHistory history;

    @Test
    void publicStatusAndHistoryAreAvailableWhileDiscordIsDisabled() throws Exception {
        mockMvc.perform(get("/api/v1/chat/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discordConfigured").value(false))
                .andExpect(jsonPath("$.discordConnected").value(false));

        mockMvc.perform(get("/api/v1/chat/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void sendingRequiresAuthenticationAndReturnsProblemDetails() throws Exception {
        mockMvc.perform(post("/api/v1/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hello\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void authenticatedUserCannotSendWhenDiscordIsNotConfigured() throws Exception {
        mockMvc.perform(post("/api/v1/chat/messages")
                        .with(user("player").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hello\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("DISCORD_UNAVAILABLE"));
    }

    @Test
    void validatesMessagesAndSupportsPollingAfterAnId() throws Exception {
        mockMvc.perform(post("/api/v1/chat/messages")
                        .with(user("player").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        mockMvc.perform(get("/api/v1/chat/messages").param("afterId", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        mockMvc.perform(post("/api/v1/chat/messages")
                        .with(user("player").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"%s\"}".formatted("x".repeat(501))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        var first = history.publish("DiscordUser", "first", Origin.DISCORD);
        var second = history.publish("MinecraftUser", "second", Origin.MINECRAFT);

        mockMvc.perform(get("/api/v1/chat/messages").param("afterId", Long.toString(first.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(second.id()))
                .andExpect(jsonPath("$[0].user").value("MinecraftUser"))
                .andExpect(jsonPath("$[0].origin").value("MINECRAFT"));
    }

    @Test
    void historyRetainsOnlyTheLatestHundredMessages() {
        for (int index = 0; index < 101; index++) {
            history.publish("DiscordUser", "message-" + index, Origin.DISCORD);
        }

        var messages = history.after(0);
        org.junit.jupiter.api.Assertions.assertEquals(100, messages.size());
        org.junit.jupiter.api.Assertions.assertEquals("message-100", messages.get(messages.size() - 1).message());
    }
}
