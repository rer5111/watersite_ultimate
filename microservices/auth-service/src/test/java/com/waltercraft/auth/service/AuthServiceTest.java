package com.waltercraft.auth.service;

import com.waltercraft.auth.dto.AuthenticatedUser;
import com.waltercraft.auth.dto.DeviceCodeResponse;
import com.waltercraft.auth.dto.DeviceLoginResponse;
import com.waltercraft.auth.dto.MinecraftProfile;
import com.waltercraft.auth.dto.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CredentialVerifier credentialVerifier;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private MinecraftOAuthClient minecraftOAuthClient;

    @Mock
    private PlayerServiceClient playerServiceClient;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                credentialVerifier,
                jwtTokenService,
                minecraftOAuthClient,
                playerServiceClient,
                notificationServiceClient
        );
    }

    @Test
    void loginWithMicrosoft_exchangesCodeAndIssuesJwtTokens() {
        UUID uuid = UUID.randomUUID();
        MinecraftProfile profile = new MinecraftProfile(uuid, "Alex");
        AuthenticatedUser user = new AuthenticatedUser(10L, "Alex", uuid, Set.of("USER"));
        TokenResponse tokenResponse = new TokenResponse("access-jwt", "refresh-jwt", "Bearer", 900);

        when(minecraftOAuthClient.exchangeCodeForMinecraftProfile("code-1", "http://redirect"))
                .thenReturn(profile);
        when(playerServiceClient.loginWithMinecraft(uuid, "Alex"))
                .thenReturn(user);
        when(jwtTokenService.issueTokens(user))
                .thenReturn(tokenResponse);

        TokenResponse result = authService.loginWithMicrosoft("code-1", "http://redirect");

        assertThat(result).isEqualTo(tokenResponse);
        verify(minecraftOAuthClient).exchangeCodeForMinecraftProfile("code-1", "http://redirect");
        verify(playerServiceClient).loginWithMinecraft(uuid, "Alex");
        verify(jwtTokenService).issueTokens(user);
    }

    @Test
    void linkMinecraft_exchangesCodeAndLinksToUser() {
        UUID uuid = UUID.randomUUID();
        MinecraftProfile profile = new MinecraftProfile(uuid, "Alex");

        when(minecraftOAuthClient.exchangeCodeForMinecraftProfile("code-link", "http://redirect"))
                .thenReturn(profile);

        authService.linkMinecraft(15L, "code-link", "http://redirect");

        verify(playerServiceClient).linkMinecraft(15L, uuid, "Alex");
    }

    @Test
    void pollDeviceLogin_whenPending_returnsPendingStatus() {
        when(minecraftOAuthClient.pollDeviceToken("dev-code-1"))
                .thenReturn(Optional.empty());

        DeviceLoginResponse response = authService.pollDeviceLogin("dev-code-1");

        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.tokens()).isNull();
    }

    @Test
    void pollDeviceLogin_whenApproved_exchangesProfileAndIssuesTokens() {
        UUID uuid = UUID.randomUUID();
        MinecraftProfile profile = new MinecraftProfile(uuid, "Alex");
        AuthenticatedUser user = new AuthenticatedUser(10L, "Alex", uuid, Set.of("USER"));
        TokenResponse tokenResponse = new TokenResponse("access-jwt", "refresh-jwt", "Bearer", 900);

        when(minecraftOAuthClient.pollDeviceToken("dev-code-2"))
                .thenReturn(Optional.of("ms-access-token"));
        when(minecraftOAuthClient.exchangeMsAccessTokenForMinecraftProfile("ms-access-token"))
                .thenReturn(profile);
        when(playerServiceClient.loginWithMinecraft(uuid, "Alex"))
                .thenReturn(user);
        when(jwtTokenService.issueTokens(user))
                .thenReturn(tokenResponse);

        DeviceLoginResponse response = authService.pollDeviceLogin("dev-code-2");

        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.tokens()).isEqualTo(tokenResponse);
    }
}
