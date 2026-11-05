package com.waltercraft.auth.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.waltercraft.auth.dto.DeviceCodeResponse;
import com.waltercraft.auth.dto.DeviceLoginRequest;
import com.waltercraft.auth.dto.DeviceLoginResponse;
import com.waltercraft.auth.dto.LoginRequest;
import com.waltercraft.auth.dto.MicrosoftAuthorizeUrlResponse;
import com.waltercraft.auth.dto.MicrosoftLoginRequest;
import com.waltercraft.auth.dto.RefreshRequest;
import com.waltercraft.auth.dto.RegisterSendCodeRequest;
import com.waltercraft.auth.dto.RegisterVerifyCodeRequest;
import com.waltercraft.auth.dto.ResendCodeRequest;
import com.waltercraft.auth.dto.SendCodeResponse;
import com.waltercraft.auth.dto.TokenResponse;
import com.waltercraft.auth.exception.InvalidCredentialsException;
import com.waltercraft.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping(path = "/auth/microsoft/authorize-url", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MicrosoftAuthorizeUrlResponse> getMicrosoftAuthorizeUrl(
            @RequestParam(name = "redirect_uri", required = false) String redirectUri,
            @RequestParam(name = "state", required = false) String state
    ) {
        String url = authService.getMicrosoftAuthorizeUrl(redirectUri, state);
        return ResponseEntity.ok(new MicrosoftAuthorizeUrlResponse(url));
    }

    @PostMapping(path = "/auth/microsoft/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenResponse> loginWithMicrosoft(@Valid @RequestBody MicrosoftLoginRequest request) {
        return ResponseEntity.ok(authService.loginWithMicrosoft(request.code(), request.redirectUri()));
    }

    @PostMapping(path = "/auth/microsoft/link", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> linkMicrosoft(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody MicrosoftLoginRequest request
    ) {
        Long userId = jwt != null && jwt.getClaim("user_id") != null
                ? ((Number) jwt.getClaim("user_id")).longValue()
                : null;
        if (userId == null) {
            throw new InvalidCredentialsException();
        }
        authService.linkMinecraft(userId, request.code(), request.redirectUri());
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/auth/microsoft/device-code", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DeviceCodeResponse> getMicrosoftDeviceCode() {
        return ResponseEntity.ok(authService.initiateDeviceCode());
    }

    @PostMapping(path = "/auth/microsoft/device-login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DeviceLoginResponse> loginWithMicrosoftDevice(@Valid @RequestBody DeviceLoginRequest request) {
        return ResponseEntity.ok(authService.pollDeviceLogin(request.deviceCode()));
    }

    @PostMapping(path = "/auth/microsoft/device-link", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DeviceLoginResponse> linkMicrosoftDevice(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody DeviceLoginRequest request
    ) {
        Long userId = jwt != null && jwt.getClaim("user_id") != null
                ? ((Number) jwt.getClaim("user_id")).longValue()
                : null;
        if (userId == null) {
            throw new InvalidCredentialsException();
        }
        return ResponseEntity.ok(authService.pollDeviceLink(userId, request.deviceCode()));
    }

    @PostMapping(path = "/auth/register/send-code", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SendCodeResponse> sendRegistrationCode(@Valid @RequestBody RegisterSendCodeRequest request) {
        return ResponseEntity.ok(authService.sendRegistrationCode(request));
    }

    @PostMapping(path = "/auth/register/verify-code", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenResponse> verifyRegistrationCode(@Valid @RequestBody RegisterVerifyCodeRequest request) {
        return ResponseEntity.ok(authService.completeRegistration(request));
    }

    @PostMapping(path = "/auth/resend-code", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SendCodeResponse> resendCode(@Valid @RequestBody ResendCodeRequest request) {
        return ResponseEntity.ok(authService.resendCode(request));
    }

    @GetMapping(path = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> jwks() {
        return publicJwkSet.toJSONObject();
    }
}
