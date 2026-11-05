package com.waltercraft.auth.service;

import com.waltercraft.auth.dto.AuthenticatedUser;
import com.waltercraft.auth.dto.ChangeEmailRequest;
import com.waltercraft.auth.dto.ChangePasswordRequest;
import com.waltercraft.auth.dto.DeviceCodeResponse;
import com.waltercraft.auth.dto.DeviceLoginResponse;
import com.waltercraft.auth.dto.LoginRequest;
import com.waltercraft.auth.dto.MinecraftProfile;
import com.waltercraft.auth.dto.RegisterSendCodeRequest;
import com.waltercraft.auth.dto.RegisterVerifyCodeRequest;
import com.waltercraft.auth.dto.ResendCodeRequest;
import com.waltercraft.auth.dto.SendCodeResponse;
import com.waltercraft.auth.dto.TokenResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final CredentialVerifier credentialVerifier;
    private final JwtTokenService jwtTokenService;
    private final MinecraftOAuthClient minecraftOAuthClient;
    private final PlayerServiceClient playerServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public AuthService(
            CredentialVerifier credentialVerifier,
            JwtTokenService jwtTokenService,
            MinecraftOAuthClient minecraftOAuthClient,
            PlayerServiceClient playerServiceClient,
            NotificationServiceClient notificationServiceClient
    ) {
        this.credentialVerifier = credentialVerifier;
        this.jwtTokenService = jwtTokenService;
        this.minecraftOAuthClient = minecraftOAuthClient;
        this.playerServiceClient = playerServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    public TokenResponse login(LoginRequest request) {
        AuthenticatedUser user = credentialVerifier.verify(request.username(), request.password());
        return jwtTokenService.issueTokens(user);
    }

    public TokenResponse refresh(String refreshToken) {
        return jwtTokenService.refresh(refreshToken);
    }

    public String getMicrosoftAuthorizeUrl(String redirectUri, String state) {
        return minecraftOAuthClient.buildAuthorizeUrl(redirectUri, state);
    }

    public TokenResponse loginWithMicrosoft(String code, String redirectUri) {
        MinecraftProfile profile = minecraftOAuthClient.exchangeCodeForMinecraftProfile(code, redirectUri);
        AuthenticatedUser user = playerServiceClient.loginWithMinecraft(profile.uuid(), profile.username());
        if (user.uuid() == null) {
            user = new AuthenticatedUser(user.id(), user.username(), profile.uuid(), user.roles());
        }
        return jwtTokenService.issueTokens(user);
    }

    public void linkMinecraft(long userId, String code, String redirectUri) {
        MinecraftProfile profile = minecraftOAuthClient.exchangeCodeForMinecraftProfile(code, redirectUri);
        playerServiceClient.linkMinecraft(userId, profile.uuid(), profile.username());
    }

    public DeviceCodeResponse initiateDeviceCode() {
        return minecraftOAuthClient.initiateDeviceCodeFlow();
    }

    public DeviceLoginResponse pollDeviceLogin(String deviceCode) {
        Optional<String> msAccessToken = minecraftOAuthClient.pollDeviceToken(deviceCode);
        if (msAccessToken.isEmpty()) {
            return new DeviceLoginResponse("PENDING", null);
        }
        MinecraftProfile profile = minecraftOAuthClient.exchangeMsAccessTokenForMinecraftProfile(msAccessToken.get());
        AuthenticatedUser user = playerServiceClient.loginWithMinecraft(profile.uuid(), profile.username());
        if (user.uuid() == null) {
            user = new AuthenticatedUser(user.id(), user.username(), profile.uuid(), user.roles());
        }
        TokenResponse tokens = jwtTokenService.issueTokens(user);
        return new DeviceLoginResponse("SUCCESS", tokens);
    }

    public DeviceLoginResponse pollDeviceLink(long userId, String deviceCode) {
        Optional<String> msAccessToken = minecraftOAuthClient.pollDeviceToken(deviceCode);
        if (msAccessToken.isEmpty()) {
            return new DeviceLoginResponse("PENDING", null);
        }
        MinecraftProfile profile = minecraftOAuthClient.exchangeMsAccessTokenForMinecraftProfile(msAccessToken.get());
        playerServiceClient.linkMinecraft(userId, profile.uuid(), profile.username());
        return new DeviceLoginResponse("SUCCESS", null);
    }

    public SendCodeResponse sendRegistrationCode(RegisterSendCodeRequest request) {
        playerServiceClient.checkAvailability(request.email(), request.username());
        return notificationServiceClient.sendCode(request.email(), "REGISTRATION");
    }

    public TokenResponse completeRegistration(RegisterVerifyCodeRequest request) {
        notificationServiceClient.verifyCode(request.email(), request.code(), "REGISTRATION");
        AuthenticatedUser user = playerServiceClient.register(request.email(), request.username(), request.password());
        return jwtTokenService.issueTokens(user);
    }

    public SendCodeResponse resendCode(ResendCodeRequest request) {
        return notificationServiceClient.sendCode(request.email(), request.purpose());
    }

    public SendCodeResponse sendEmailChangeCode(long userId, String newEmail) {
        playerServiceClient.checkAvailability(newEmail, null);
        return notificationServiceClient.sendCode(newEmail, "CHANGE_EMAIL");
    }

    public void changeEmail(long userId, ChangeEmailRequest request) {
        notificationServiceClient.verifyCode(request.newEmail(), request.code(), "CHANGE_EMAIL");
        playerServiceClient.changeEmail(userId, request.newEmail());
    }

    public SendCodeResponse sendPasswordChangeCode(long userId) {
        String email = playerServiceClient.getUserEmail(userId);
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("К аккаунту не привязана почта. Сначала укажите email в профиле.");
        }
        return notificationServiceClient.sendCode(email, "CHANGE_PASSWORD");
    }

    public void changePassword(long userId, ChangePasswordRequest request) {
        String email = playerServiceClient.getUserEmail(userId);
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("К аккаунту не привязана почта. Сначала укажите email в профиле.");
        }
        notificationServiceClient.verifyCode(email, request.code(), "CHANGE_PASSWORD");
        playerServiceClient.changePassword(userId, request.newPassword());
    }
}
