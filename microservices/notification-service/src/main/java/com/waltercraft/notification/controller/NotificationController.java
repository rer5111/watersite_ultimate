package com.waltercraft.notification.controller;

import com.waltercraft.notification.dto.UnreadCountResponse;
import com.waltercraft.notification.dto.UserNotificationResponse;
import com.waltercraft.notification.service.UserNotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final UserNotificationService userNotificationService;

    public NotificationController(UserNotificationService userNotificationService) {
        this.userNotificationService = userNotificationService;
    }

    @GetMapping
    public List<UserNotificationResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return userNotificationService.list(userId(jwt), roles(jwt));
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse unreadCount(@AuthenticationPrincipal Jwt jwt) {
        return userNotificationService.unreadCount(userId(jwt), roles(jwt));
    }

    @PostMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        if (!userNotificationService.markRead(userId(jwt), roles(jwt), id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Уведомление не найдено");
        }
    }

    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(@AuthenticationPrincipal Jwt jwt) {
        userNotificationService.markAllRead(userId(jwt), roles(jwt));
    }

    private long userId(Jwt jwt) {
        Object value = jwt.getClaim("user_id");
        if (value instanceof Number number && number.longValue() > 0) {
            return number.longValue();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Не удалось определить пользователя");
    }

    private Set<String> roles(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles == null ? Set.of() : Set.copyOf(roles);
    }
}
