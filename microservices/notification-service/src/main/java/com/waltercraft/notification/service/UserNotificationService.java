package com.waltercraft.notification.service;

import com.waltercraft.notification.dto.UnreadCountResponse;
import com.waltercraft.notification.dto.UserNotificationResponse;
import com.waltercraft.notification.entity.Broadcast;
import com.waltercraft.notification.entity.BroadcastAudience;
import com.waltercraft.notification.entity.BroadcastStatus;
import com.waltercraft.notification.entity.NotificationRead;
import com.waltercraft.notification.repository.BroadcastRepository;
import com.waltercraft.notification.repository.NotificationReadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class UserNotificationService {

    private static final String ROLE_ORGANIZER = "ORGANIZER";
    private static final String ROLE_MODERATOR = "MODERATOR";
    private static final String ROLE_ADMIN = "ADMIN";

    private final BroadcastRepository broadcastRepository;
    private final NotificationReadRepository notificationReadRepository;

    public UserNotificationService(
            BroadcastRepository broadcastRepository,
            NotificationReadRepository notificationReadRepository
    ) {
        this.broadcastRepository = broadcastRepository;
        this.notificationReadRepository = notificationReadRepository;
    }

    public List<UserNotificationResponse> list(long userId, Set<String> roles) {
        List<Broadcast> broadcasts = visibleBroadcasts(roles);
        Set<Long> readIds = notificationReadRepository.findBroadcastIdsByUserId(userId);
        return broadcasts.stream()
                .map(broadcast -> UserNotificationResponse.from(broadcast, readIds.contains(broadcast.getId())))
                .toList();
    }

    public UnreadCountResponse unreadCount(long userId, Set<String> roles) {
        Set<Long> readIds = notificationReadRepository.findBroadcastIdsByUserId(userId);
        long count = visibleBroadcasts(roles).stream()
                .filter(broadcast -> !readIds.contains(broadcast.getId()))
                .count();
        return new UnreadCountResponse(count);
    }

    @Transactional
    public boolean markRead(long userId, Set<String> roles, long broadcastId) {
        boolean visible = broadcastRepository.findById(broadcastId)
                .filter(broadcast -> broadcast.getStatus() == BroadcastStatus.SENT)
                .filter(broadcast -> audiences(roles).contains(broadcast.getAudience()))
                .isPresent();
        if (!visible) {
            return false;
        }
        if (!notificationReadRepository.existsByUserIdAndBroadcastId(userId, broadcastId)) {
            notificationReadRepository.save(new NotificationRead(userId, broadcastId, Instant.now()));
        }
        return true;
    }

    @Transactional
    public void markAllRead(long userId, Set<String> roles) {
        Set<Long> readIds = notificationReadRepository.findBroadcastIdsByUserId(userId);
        List<NotificationRead> unread = visibleBroadcasts(roles).stream()
                .filter(broadcast -> !readIds.contains(broadcast.getId()))
                .map(broadcast -> new NotificationRead(userId, broadcast.getId(), Instant.now()))
                .toList();
        notificationReadRepository.saveAll(unread);
    }

    private List<Broadcast> visibleBroadcasts(Set<String> roles) {
        return broadcastRepository.findVisible(audiences(roles), BroadcastStatus.SENT);
    }

    private Set<BroadcastAudience> audiences(Set<String> roles) {
        Set<BroadcastAudience> audiences = EnumSet.of(BroadcastAudience.ALL);
        if (roles.contains(ROLE_ORGANIZER)) {
            audiences.add(BroadcastAudience.ORGANIZERS);
        }
        if (roles.contains(ROLE_MODERATOR) || roles.contains(ROLE_ADMIN)) {
            audiences.add(BroadcastAudience.MODERATORS);
        }
        if (roles.contains(ROLE_ADMIN)) {
            audiences.add(BroadcastAudience.ADMINS);
        }
        return audiences;
    }
}
