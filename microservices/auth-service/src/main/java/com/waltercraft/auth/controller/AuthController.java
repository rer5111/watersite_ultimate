package com.waltercraft.auth.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.waltercraft.auth.dto.LoginRequest;
import com.waltercraft.auth.dto.RefreshRequest;
import com.waltercraft.auth.dto.TokenResponse;
import com.waltercraft.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    private final AuthService authService;
    private final JWKSet publicJwkSet;

    public AuthController(AuthService authService, JWKSet publicJwkSet) {
        this.authService = authService;
        this.publicJwkSet = publicJwkSet;
    }

    @PostMapping(path = "/auth/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping(path = "/auth/refresh", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @GetMapping(path = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> jwks() {
        return publicJwkSet.toJSONObject();
    }
}
