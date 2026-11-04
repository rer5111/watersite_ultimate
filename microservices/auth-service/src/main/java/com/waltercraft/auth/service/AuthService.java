package com.waltercraft.auth.service;

import com.waltercraft.auth.dto.AuthenticatedUser;
import com.waltercraft.auth.dto.LoginRequest;
import com.waltercraft.auth.dto.TokenResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final CredentialVerifier credentialVerifier;
    private final JwtTokenService jwtTokenService;

    public AuthService(CredentialVerifier credentialVerifier, JwtTokenService jwtTokenService) {
        this.credentialVerifier = credentialVerifier;
        this.jwtTokenService = jwtTokenService;
    }

    public TokenResponse login(LoginRequest request) {
        AuthenticatedUser user = credentialVerifier.verify(request.username(), request.password());
        return jwtTokenService.issueTokens(user);
    }

    public TokenResponse refresh(String refreshToken) {
        return jwtTokenService.refresh(refreshToken);
    }
}
