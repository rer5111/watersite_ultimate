package com.waltercraft.notification.controller;

import com.waltercraft.notification.dto.SendCodeRequest;
import com.waltercraft.notification.dto.SendCodeResponse;
import com.waltercraft.notification.dto.VerifyCodeRequest;
import com.waltercraft.notification.dto.VerifyCodeResponse;
import com.waltercraft.notification.service.VerificationCodeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/internal/notifications/email")
public class InternalNotificationController {

    private final VerificationCodeService verificationCodeService;
    private final String sharedSecret;

    public InternalNotificationController(
            VerificationCodeService verificationCodeService,
            @Value("${security.notification-service.shared-secret:}") String sharedSecret
    ) {
        this.verificationCodeService = verificationCodeService;
        this.sharedSecret = sharedSecret == null ? "" : sharedSecret;
    }

    @PostMapping("/send-code")
    public ResponseEntity<SendCodeResponse> sendCode(
            @RequestHeader(name = "X-Internal-Auth", required = false) String providedSecret,
            @Valid @RequestBody SendCodeRequest request
    ) {
        if (!isAuthorized(providedSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        SendCodeResponse response = verificationCodeService.generateAndSendCode(request.email(), request.purpose());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-code")
    public ResponseEntity<VerifyCodeResponse> verifyCode(
            @RequestHeader(name = "X-Internal-Auth", required = false) String providedSecret,
            @Valid @RequestBody VerifyCodeRequest request
    ) {
        if (!isAuthorized(providedSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        VerifyCodeResponse response = verificationCodeService.verifyCode(request.email(), request.code(), request.purpose());
        return ResponseEntity.ok(response);
    }

    private boolean isAuthorized(String providedSecret) {
        if (sharedSecret.isBlank() || providedSecret == null) {
            return false;
        }
        return MessageDigest.isEqual(
                providedSecret.getBytes(StandardCharsets.UTF_8),
                sharedSecret.getBytes(StandardCharsets.UTF_8)
        );
    }
}
