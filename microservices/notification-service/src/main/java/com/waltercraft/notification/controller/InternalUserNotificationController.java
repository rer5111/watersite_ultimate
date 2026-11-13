package com.waltercraft.notification.controller;

import com.waltercraft.notification.dto.CreateUserNotificationRequest;
import com.waltercraft.notification.dto.UserNotificationResponse;
import com.waltercraft.notification.entity.Broadcast;
import com.waltercraft.notification.entity.BroadcastAudience;
import com.waltercraft.notification.entity.BroadcastStatus;
import com.waltercraft.notification.repository.BroadcastRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@RestController
@RequestMapping("/internal/notifications/user")
public class InternalUserNotificationController {

    private final BroadcastRepository broadcastRepository;
    private final String sharedSecret;

    public InternalUserNotificationController(
            BroadcastRepository broadcastRepository,
            @Value("${security.notification-service.shared-secret:}") String sharedSecret
    ) {
        this.broadcastRepository = broadcastRepository;
        this.sharedSecret = sharedSecret == null ? "" : sharedSecret;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<UserNotificationResponse> createNotification(
            @RequestHeader(name = "X-Internal-Auth", required = false) String providedSecret,
            @Valid @RequestBody CreateUserNotificationRequest request
    ) {
        if (!isAuthorized(providedSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Broadcast notification = new Broadcast(
                request.subject(),
                request.category(),
                BroadcastAudience.ALL,
                request.message(),
                Instant.now(),
                1,
                BroadcastStatus.SENT,
                request.userId()
        );
        Broadcast saved = broadcastRepository.save(notification);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserNotificationResponse.from(saved, false));
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
