package com.waltercraft.auth.service;

import com.waltercraft.auth.config.MicrosoftOAuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MinecraftOAuthClientTest {

    private MicrosoftOAuthProperties properties;
    private MinecraftOAuthClient client;

    @BeforeEach
    void setUp() {
        properties = new MicrosoftOAuthProperties(
                "test-client-id",
                "test-client-secret",
                "http://localhost:5173/auth/microsoft/callback",
                "XboxLive.signin offline_access",
                false,
                "c36a9fb6-4f2a-41ff-90bd-ae7cc92031eb"
        );
        client = new MinecraftOAuthClient(properties);
    }

    @Test
    void buildAuthorizeUrl_containsExpectedQueryParams() {
        String url = client.buildAuthorizeUrl(null, "state-xyz");

        assertThat(url).startsWith("https://login.live.com/oauth20_authorize.srf?");
        assertThat(url).contains("client_id=test-client-id");
        assertThat(url).contains("response_type=code");
        assertThat(url).contains("redirect_uri=http%3A%2F%2Flocalhost%3A5173%2Fauth%2Fmicrosoft%2Fcallback");
        assertThat(url).contains("state=state-xyz");
        assertThat(url).contains("prompt=select_account");
    }

    @Test
    void parseMojangUuid_convertsUndashedHexStringToUuid() {
        String hex = "ccb40ba9e0af49399c803ce15fb417ed";
        UUID expected = UUID.fromString("ccb40ba9-e0af-4939-9c80-3ce15fb417ed");

        UUID actual = MinecraftOAuthClient.parseMojangUuid(hex);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseMojangUuid_throwsOnInvalidLength() {
        assertThatThrownBy(() -> MinecraftOAuthClient.parseMojangUuid("invalid-too-short"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
