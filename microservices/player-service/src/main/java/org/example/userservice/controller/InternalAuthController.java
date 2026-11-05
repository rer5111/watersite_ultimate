package org.example.userservice.controller;

import org.example.userservice.dto.CheckAvailabilityRequest;
import org.example.userservice.dto.CheckAvailabilityResponse;
import org.example.userservice.dto.InternalChangeEmailRequest;
import org.example.userservice.dto.InternalChangePasswordRequest;
import org.example.userservice.dto.InternalCredentialRequest;
import org.example.userservice.dto.InternalCredentialVerificationResponse;
import org.example.userservice.dto.InternalRegisterRequest;
import org.example.userservice.dto.LinkMinecraftRequest;
import org.example.userservice.dto.LinkMinecraftResponse;
import org.example.userservice.dto.MinecraftLoginRequest;
import org.example.userservice.model.RoleEnum;
import org.example.userservice.model.UserModel;
import org.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/auth")
public class InternalAuthController {

    private static final int MINIMUM_SECRET_LENGTH = 32;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final String sharedSecret;

    public InternalAuthController(
            UserService userService,
            PasswordEncoder passwordEncoder,
            @Value("${security.auth-service.shared-secret:}") String sharedSecret
    ) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.sharedSecret = sharedSecret == null ? "" : sharedSecret;
    }

    @PostMapping("/verify")
    public ResponseEntity<InternalCredentialVerificationResponse> verifyCredentials(
            @RequestHeader(name = "X-Internal-Auth", required = false) String providedSecret,
            @Valid @RequestBody InternalCredentialRequest request
    ) {
        if (isSecretMisconfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        if (!matchesSecret(providedSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<UserModel> matches = userService.findByIdentifier(request.username());
        if (matches.size() != 1) {
            return ResponseEntity.ok(InternalCredentialVerificationResponse.rejected());
        }

        UserModel user = matches.get(0);
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.ok(InternalCredentialVerificationResponse.rejected());
        }

        Set<String> roles = user.getRoles() == null
                ? Set.of()
                : user.getRoles().stream().map(RoleEnum::name).collect(Collectors.toUnmodifiableSet());
        UUID uuid = user.getAccount() != null ? user.getAccount().getUuid() : null;
        return ResponseEntity.ok(new InternalCredentialVerificationResponse(true, user.getId(), user.getName(), roles, uuid));
    }

    @PostMapping("/minecraft-login")
    public ResponseEntity<InternalCredentialVerificationResponse> loginWithMinecraft(
            @RequestHeader(name = "X-Internal-Auth", required = false) String providedSecret,
            @Valid @RequestBody MinecraftLoginRequest request
    ) {
        if (isSecretMisconfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        if (!matchesSecret(providedSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserModel user = userService.loginOrRegisterWithMinecraft(request.uuid(), request.username());
        Set<String> roles = user.getRoles() == null
                ? Set.of()
                : user.getRoles().stream().map(RoleEnum::name).collect(Collectors.toUnmodifiableSet());
        UUID uuid = user.getAccount() != null && user.getAccount().getUuid() != null ? user.getAccount().getUuid() : request.uuid();
        return ResponseEntity.ok(new InternalCredentialVerificationResponse(true, user.getId(), user.getName(), roles, uuid));
    }

    @PostMapping("/link-minecraft")
    public ResponseEntity<LinkMinecraftResponse> linkMinecraft(
            @RequestHeader(name = "X-Internal-Auth", required = false) String providedSecret,
            @Valid @RequestBody LinkMinecraftRequest request
    ) {
        if (isSecretMisconfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        if (!matchesSecret(providedSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            userService.linkMinecraft(request.userId(), request.uuid(), request.username());
            return ResponseEntity.ok(LinkMinecraftResponse.success(request.userId(), request.uuid(), request.username()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(LinkMinecraftResponse.failure(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(LinkMinecraftResponse.failure(e.getMessage()));
        }
    }

    @PostMapping("/check-availability")
    public ResponseEntity<CheckAvailabilityResponse> checkAvailability(
            @RequestHeader(name = "X-Internal-Auth", required = false) String providedSecret,
            @Valid @RequestBody CheckAvailabilityRequest request
    ) {
        if (isSecretMisconfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        if (!matchesSecret(providedSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.checkAvailability(request.email(), request.username()));
    }

    @PostMapping("/register")
    public ResponseEntity<InternalCredentialVerificationResponse> register(
            @RequestHeader(name = "X-Internal-Auth", required = false) String providedSecret,
            @Valid @RequestBody InternalRegisterRequest request
    ) {
        if (isSecretMisconfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        if (!matchesSecret(providedSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            UserModel user = userService.registerUser(request.email(), request.username(), request.password());
            Set<String> roles = user.getRoles() == null
                    ? Set.of()
                    : user.getRoles().stream().map(RoleEnum::name).collect(Collectors.toUnmodifiableSet());
            UUID uuid = user.getAccount() != null ? user.getAccount().getUuid() : null;
            return ResponseEntity.ok(new InternalCredentialVerificationResponse(true, user.getId(), user.getName(), roles, uuid));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/change-email")
    public ResponseEntity<Void> changeEmail(
            @RequestHeader(name = "X-Internal-Auth", required = false) String providedSecret,
            @Valid @RequestBody InternalChangeEmailRequest request
    ) {
        if (isSecretMisconfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        if (!matchesSecret(providedSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            userService.changeEmail(request.userId(), request.newEmail());
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestHeader(name = "X-Internal-Auth", required = false) String providedSecret,
            @Valid @RequestBody InternalChangePasswordRequest request
    ) {
        if (isSecretMisconfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        if (!matchesSecret(providedSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            userService.changePassword(request.userId(), request.newPassword());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/user-email/{userId}")
    public ResponseEntity<String> getUserEmail(
            @RequestHeader(name = "X-Internal-Auth", required = false) String providedSecret,
            @PathVariable long userId
    ) {
        if (isSecretMisconfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        if (!matchesSecret(providedSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            String email = userService.getUserEmail(userId);
            if (email == null || email.isBlank()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.ok(email);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private boolean isSecretMisconfigured() {
        return sharedSecret.length() < MINIMUM_SECRET_LENGTH;
    }

    private boolean matchesSecret(String providedSecret) {
        if (isSecretMisconfigured() || providedSecret == null || providedSecret.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                sharedSecret.getBytes(StandardCharsets.UTF_8),
                providedSecret.getBytes(StandardCharsets.UTF_8)
        );
    }
}
