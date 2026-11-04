package com.waltercraft.auth.service;

import com.waltercraft.auth.dto.AuthenticatedUser;
import com.waltercraft.auth.dto.CredentialVerificationResponse;
import com.waltercraft.auth.dto.InternalCredentialRequest;
import com.waltercraft.auth.exception.InvalidCredentialsException;
import com.waltercraft.auth.exception.PlayerServiceUnavailableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class PlayerServiceCredentialVerifier implements CredentialVerifier {

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
        return new AuthenticatedUser(response.userId(), response.username(), response.roles());
    }
}
