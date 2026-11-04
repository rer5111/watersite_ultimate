package org.example.userservice.controller;

import org.example.userservice.dto.InternalCredentialRequest;
import org.example.userservice.dto.InternalCredentialVerificationResponse;
import org.example.userservice.model.RoleEnum;
import org.example.userservice.model.UserModel;
import org.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
        if (sharedSecret.length() < MINIMUM_SECRET_LENGTH) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        if (!matchesSecret(providedSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<UserModel> matches = userService.findByName(request.username());
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
        return ResponseEntity.ok(new InternalCredentialVerificationResponse(true, user.getId(), user.getName(), roles));
    }

    private boolean matchesSecret(String providedSecret) {
        if (providedSecret == null) {
            return false;
        }
        return MessageDigest.isEqual(
                sharedSecret.getBytes(StandardCharsets.UTF_8),
                providedSecret.getBytes(StandardCharsets.UTF_8)
        );
    }
}
