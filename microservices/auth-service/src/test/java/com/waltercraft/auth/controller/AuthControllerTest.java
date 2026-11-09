package com.waltercraft.auth.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.waltercraft.auth.config.JwtConfiguration;
import com.waltercraft.auth.config.JwtProperties;
import com.waltercraft.auth.config.JwtSigningKey;
import com.waltercraft.auth.dto.LoginRequest;
import com.waltercraft.auth.dto.TokenResponse;
import com.waltercraft.auth.exception.ApiExceptionHandler;
import com.waltercraft.auth.exception.InvalidCredentialsException;
import com.waltercraft.auth.service.AuthService;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                "https://auth.test",
                "",
                true,
                Duration.ofMinutes(15),
                Duration.ofDays(7)
        );
        JwtConfiguration jwtConfiguration = new JwtConfiguration();
        JwtSigningKey signingKey = jwtConfiguration.jwtSigningKey(properties);
        RSAKey jwk = jwtConfiguration.jwk(signingKey);
        JWKSet publicJwkSet = jwtConfiguration.publicJwkSet(jwk);
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService, publicJwkSet))
                .setValidator(validator)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void loginReturnsBearerTokenPair() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new TokenResponse("access", "refresh", "Bearer", 900));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"player","password":"correct"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void loginRejectsInvalidCredentialsWithProblemDetails() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"player","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"))
                .andExpect(jsonPath("$.details").isMap());
    }

    @Test
    void refreshIssuesNewTokenPair() throws Exception {
        when(authService.refresh("valid-refresh"))
                .thenReturn(new TokenResponse("new-access", "new-refresh", "Bearer", 900));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"valid-refresh"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
    }

    @Test
    void publishesPublicOnlyJwks() throws Exception {
        mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].alg").value("RS256"))
                .andExpect(jsonPath("$.keys[0].d").doesNotExist());
    }

    @Test
    void validatesLoginBody() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":" ","password":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void getMicrosoftAuthorizeUrl_returnsUrl() throws Exception {
        when(authService.getMicrosoftAuthorizeUrl("http://localhost:5173/callback", "state123"))
                .thenReturn("https://login.live.com/oauth20_authorize.srf?client_id=test");

        mockMvc.perform(get("/auth/microsoft/authorize-url")
                        .param("redirect_uri", "http://localhost:5173/callback")
                        .param("state", "state123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://login.live.com/oauth20_authorize.srf?client_id=test"));
    }

    @Test
    void loginWithMicrosoft_returnsTokenPair() throws Exception {
        when(authService.loginWithMicrosoft("auth-code-123", "http://localhost:5173/callback"))
                .thenReturn(new TokenResponse("ms-access", "ms-refresh", "Bearer", 900));

        mockMvc.perform(post("/auth/microsoft/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"auth-code-123","redirectUri":"http://localhost:5173/callback"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("ms-access"))
                .andExpect(jsonPath("$.refreshToken").value("ms-refresh"));
    }

    @Test
    void getMicrosoftDeviceCode_returnsDeviceCodeDetails() throws Exception {
        when(authService.initiateDeviceCode())
                .thenReturn(new com.waltercraft.auth.dto.DeviceCodeResponse("dev-1", "CODE-1", "https://microsoft.com/link", 900, 5));

        mockMvc.perform(post("/auth/microsoft/device-code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceCode").value("dev-1"))
                .andExpect(jsonPath("$.userCode").value("CODE-1"))
                .andExpect(jsonPath("$.verificationUri").value("https://microsoft.com/link"));
    }

    @Test
    void loginWithMicrosoftDevice_returnsStatusAndTokens() throws Exception {
        when(authService.pollDeviceLogin("dev-1"))
                .thenReturn(new com.waltercraft.auth.dto.DeviceLoginResponse("SUCCESS", new TokenResponse("dev-access", "dev-refresh", "Bearer", 900)));

        mockMvc.perform(post("/auth/microsoft/device-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"deviceCode":"dev-1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.tokens.accessToken").value("dev-access"));
    }

    @Test
    void sendRegistrationCode_rateLimitExceeded_returns429() throws Exception {
        when(authService.sendRegistrationCode(any()))
                .thenThrow(new com.waltercraft.auth.exception.RateLimitExceededException("Запрос нового кода возможен через 45 сек.", 45));

        mockMvc.perform(post("/auth/register/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","username":"walter","password":"Password123!"}
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("RATE_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.details.retryAfterSeconds").value("45"));
    }
}
