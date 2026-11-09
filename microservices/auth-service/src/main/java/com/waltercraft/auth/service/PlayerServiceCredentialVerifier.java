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
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;

@Component
public class PlayerServiceCredentialVerifier implements CredentialVerifier, PlayerServiceClient {

    private static final Logger log = LoggerFactory.getLogger(PlayerServiceCredentialVerifier.class);

    private final RestClient playerServiceRestClient;

    public PlayerServiceCredentialVerifier(RestClient playerServiceRestClient) {
        this.playerServiceRestClient = playerServiceRestClient;
    }

    @Override
    @CircuitBreaker(name = "playerService", fallbackMethod = "verifyFallback")
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
    @CircuitBreaker(name = "playerService", fallbackMethod = "minecraftLoginFallback")
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
    @CircuitBreaker(name = "playerService", fallbackMethod = "linkMinecraftFallback")
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
    @CircuitBreaker(name = "playerService", fallbackMethod = "checkAvailabilityFallback")
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
            if (exception.getStatusCode().is4xxClientError()) {
                throw new IllegalArgumentException("Некорректные данные для проверки доступности");
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
    @CircuitBreaker(name = "playerService", fallbackMethod = "registerFallback")
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
            if (exception.getStatusCode().is4xxClientError()) {
                throw new IllegalArgumentException("Некорректные данные для проверки доступности");
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
    @CircuitBreaker(name = "playerService", fallbackMethod = "changeEmailFallback")
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
    @CircuitBreaker(name = "playerService", fallbackMethod = "changePasswordFallback")
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
    @CircuitBreaker(name = "playerService", fallbackMethod = "getUserEmailFallback")
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

    private AuthenticatedUser verifyFallback(String username, String password, Throwable cause) {
        throw fallbackFailure(cause);
    }

    private AuthenticatedUser minecraftLoginFallback(UUID uuid, String username, Throwable cause) {
        throw fallbackFailure(cause);
    }

    private void linkMinecraftFallback(long userId, UUID uuid, String username, Throwable cause) {
        throw fallbackFailure(cause);
    }

    private void checkAvailabilityFallback(String email, String username, Throwable cause) {
        throw fallbackFailure(cause);
    }

    private AuthenticatedUser registerFallback(String email, String username, String password, Throwable cause) {
        throw fallbackFailure(cause);
    }

    private void changeEmailFallback(long userId, String newEmail, Throwable cause) {
        throw fallbackFailure(cause);
    }

    private void changePasswordFallback(long userId, String newPassword, Throwable cause) {
        throw fallbackFailure(cause);
    }

    private String getUserEmailFallback(long userId, Throwable cause) {
        throw fallbackFailure(cause);
    }

    private RuntimeException fallbackFailure(Throwable cause) {
        if (cause instanceof InvalidCredentialsException
                || cause instanceof UserAlreadyExistsException
                || cause instanceof MinecraftAccountAlreadyLinkedException
                || cause instanceof IllegalArgumentException
                || cause instanceof IllegalStateException) {
            return (RuntimeException) cause;
        }
        if (cause instanceof PlayerServiceUnavailableException unavailable) {
            log.warn("Player service fallback invoked exceptionType={}", cause.getClass().getSimpleName());
            return unavailable;
        }
        log.warn("Player service fallback invoked exceptionType={}", cause.getClass().getSimpleName());
        return new PlayerServiceUnavailableException(cause);
    }
}
