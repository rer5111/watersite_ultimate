package com.waltercraft.auth.service;

import com.waltercraft.auth.dto.AuthenticatedUser;
import com.waltercraft.auth.dto.CheckAvailabilityRequest;
import com.waltercraft.auth.dto.CheckAvailabilityResponse;
import com.waltercraft.auth.dto.CredentialVerificationResponse;
import com.waltercraft.auth.dto.InternalChangeEmailRequest;
import com.waltercraft.auth.dto.InternalChangePasswordRequest;
import com.waltercraft.auth.dto.InternalCredentialRequest;
import com.waltercraft.auth.dto.InternalLinkMinecraftRequest;
import com.waltercraft.auth.dto.InternalLinkMinecraftResponse;
import com.waltercraft.auth.dto.InternalMinecraftLoginRequest;
import com.waltercraft.auth.dto.InternalRegisterRequest;
import com.waltercraft.auth.exception.InvalidCredentialsException;
import com.waltercraft.auth.exception.MinecraftAccountAlreadyLinkedException;
import com.waltercraft.auth.exception.PlayerServiceUnavailableException;
import com.waltercraft.auth.exception.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;

@Component
public class PlayerServiceCredentialVerifier implements CredentialVerifier, PlayerServiceClient {

    private final RestClient playerServiceRestClient;

    public PlayerServiceCredentialVerifier(RestClient playerServiceRestClient) {
        this.playerServiceRestClient = playerServiceRestClient;
    }

    @Override
    public AuthenticatedUser verify(String username, String password) {
        CredentialVerificationResponse response;
        try {
            response = playerServiceRestClient.post()
                    .uri("/internal/auth/verify")
                    .body(new InternalCredentialRequest(username, password))
                    .retrieve()
                    .body(CredentialVerificationResponse.class);
        } catch (RestClientException exception) {
            throw new PlayerServiceUnavailableException(exception);
        }
        if (response == null) {
            throw new PlayerServiceUnavailableException();
        }
        if (!response.authenticated()) {
            throw new InvalidCredentialsException();
        }
        return new AuthenticatedUser(response.userId(), response.username(), response.uuid(), response.roles());
    }

    @Override
    public AuthenticatedUser loginWithMinecraft(UUID uuid, String username) {
        CredentialVerificationResponse response;
        try {
            response = playerServiceRestClient.post()
                    .uri("/internal/auth/minecraft-login")
                    .body(new InternalMinecraftLoginRequest(uuid, username))
                    .retrieve()
                    .body(CredentialVerificationResponse.class);
        } catch (RestClientException exception) {
            throw new PlayerServiceUnavailableException(exception);
        }
        if (response == null || !response.authenticated()) {
            throw new PlayerServiceUnavailableException();
        }
        return new AuthenticatedUser(response.userId(), response.username(), uuid, response.roles());
    }

    @Override
    public void linkMinecraft(long userId, UUID uuid, String username) {
        try {
            playerServiceRestClient.post()
                    .uri("/internal/auth/link-minecraft")
                    .body(new InternalLinkMinecraftRequest(userId, uuid, username))
                    .retrieve()
                    .body(InternalLinkMinecraftResponse.class);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.CONFLICT) {
                throw new MinecraftAccountAlreadyLinkedException();
            }
            throw new PlayerServiceUnavailableException(exception);
        } catch (RestClientException exception) {
            throw new PlayerServiceUnavailableException(exception);
        }
    }

    @Override
    public void checkAvailability(String email, String username) {
        CheckAvailabilityResponse response;
        try {
            response = playerServiceRestClient.post()
                    .uri("/internal/auth/check-availability")
                    .body(new CheckAvailabilityRequest(email, username))
                    .retrieve()
                    .body(CheckAvailabilityResponse.class);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.CONFLICT) {
                throw new UserAlreadyExistsException("Пользователь с таким email или никнеймом уже зарегистрирован");
            }
            throw new PlayerServiceUnavailableException(exception);
        } catch (RestClientException exception) {
            throw new PlayerServiceUnavailableException(exception);
        }

        if (response != null && !response.available()) {
            throw new UserAlreadyExistsException(response.message(), response.field());
        }
    }

    @Override
    public AuthenticatedUser register(String email, String username, String password) {
        CredentialVerificationResponse response;
        try {
            response = playerServiceRestClient.post()
                    .uri("/internal/auth/register")
                    .body(new InternalRegisterRequest(email, username, password))
                    .retrieve()
                    .body(CredentialVerificationResponse.class);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.CONFLICT) {
                throw new UserAlreadyExistsException("Пользователь с таким email или никнеймом уже зарегистрирован");
            }
            throw new PlayerServiceUnavailableException(exception);
        } catch (RestClientException exception) {
            throw new PlayerServiceUnavailableException(exception);
        }
        if (response == null || !response.authenticated()) {
            throw new PlayerServiceUnavailableException();
        }
        return new AuthenticatedUser(response.userId(), response.username(), response.uuid(), response.roles());
    }

    @Override
    public void changeEmail(long userId, String newEmail) {
        try {
            playerServiceRestClient.post()
                    .uri("/internal/auth/change-email")
                    .body(new InternalChangeEmailRequest(userId, newEmail))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.CONFLICT) {
                throw new UserAlreadyExistsException("Email уже используется другим пользователем");
            }
            throw new PlayerServiceUnavailableException(exception);
        } catch (RestClientException exception) {
            throw new PlayerServiceUnavailableException(exception);
        }
    }

    @Override
    public void changePassword(long userId, String newPassword) {
        try {
            playerServiceRestClient.post()
                    .uri("/internal/auth/change-password")
                    .body(new InternalChangePasswordRequest(userId, newPassword))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new PlayerServiceUnavailableException(exception);
        }
    }

    @Override
    public String getUserEmail(long userId) {
        try {
            return playerServiceRestClient.get()
                    .uri("/internal/auth/user-email/{userId}", userId)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException exception) {
            throw new PlayerServiceUnavailableException(exception);
        }
    }
}
