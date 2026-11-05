package com.waltercraft.notification.service;

import com.waltercraft.notification.dto.UnreadCountResponse;
import com.waltercraft.notification.dto.UserNotificationResponse;
import com.waltercraft.notification.entity.Broadcast;
import com.waltercraft.notification.entity.BroadcastAudience;
import com.waltercraft.notification.entity.BroadcastCategory;
import com.waltercraft.notification.entity.BroadcastStatus;
import com.waltercraft.notification.entity.NotificationRead;
import com.waltercraft.notification.repository.BroadcastRepository;
import com.waltercraft.notification.repository.NotificationReadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserNotificationServiceTest {

    @Mock
    private BroadcastRepository broadcastRepository;

    @Mock
    private NotificationReadRepository notificationReadRepository;

    private UserNotificationService service;

    @BeforeEach
    void setUp() {
        service = new UserNotificationService(broadcastRepository, notificationReadRepository);
    }

    @Test
    void list_marksBroadcastsReadByUser() {
        Broadcast first = broadcast(1L, "Первое");
        Broadcast second = broadcast(2L, "Второе");
        when(broadcastRepository.findVisible(any(), eq(BroadcastStatus.SENT))).thenReturn(List.of(first, second));
        when(notificationReadRepository.findBroadcastIdsByUserId(109L)).thenReturn(Set.of(1L));

        List<UserNotificationResponse> result = service.list(109L, Set.of("USER"));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).read()).isTrue();
        assertThat(result.get(1).read()).isFalse();
    }

    @Test
    void unreadCount_ignoresReadBroadcasts() {
        when(broadcastRepository.findVisible(any(), eq(BroadcastStatus.SENT)))
                .thenReturn(List.of(broadcast(1L, "Первое"), broadcast(2L, "Второе")));
        when(notificationReadRepository.findBroadcastIdsByUserId(109L)).thenReturn(Set.of(2L));

        UnreadCountResponse result = service.unreadCount(109L, Set.of("USER"));

        assertThat(result.count()).isEqualTo(1);
    }

    @Test
    void list_extendsAudienceForAdminRoles() {
        when(broadcastRepository.findVisible(any(), eq(BroadcastStatus.SENT))).thenReturn(List.of());

        service.list(1L, Set.of("ADMIN"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<BroadcastAudience>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(broadcastRepository).findVisible(captor.capture(), eq(BroadcastStatus.SENT));
        assertThat(captor.getValue()).containsExactlyInAnyOrder(
                BroadcastAudience.ALL,
                BroadcastAudience.MODERATORS,
                BroadcastAudience.ADMINS
        );
    }

    @Test
    void markAllRead_savesOnlyUnreadVisibleBroadcasts() {
        when(broadcastRepository.findVisible(any(), eq(BroadcastStatus.SENT)))
                .thenReturn(List.of(broadcast(1L, "Первое"), broadcast(2L, "Второе")));
        when(notificationReadRepository.findBroadcastIdsByUserId(109L)).thenReturn(Set.of(1L));

        service.markAllRead(109L, Set.of("USER"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<NotificationRead>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationReadRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getBroadcastId()).isEqualTo(2L);
        verify(notificationReadRepository, never()).save(any());
    }

    @Test
    void markRead_savesOnlyVisibleBroadcast() {
        Broadcast broadcast = broadcast(1L, "Первое");
        when(broadcastRepository.findById(1L)).thenReturn(java.util.Optional.of(broadcast));
        when(notificationReadRepository.existsByUserIdAndBroadcastId(109L, 1L)).thenReturn(false);

        boolean marked = service.markRead(109L, Set.of("USER"), 1L);

        assertThat(marked).isTrue();
        verify(notificationReadRepository).save(any(NotificationRead.class));
    }

    @Test
    void markRead_rejectsBroadcastFromAnotherAudience() {
        Broadcast broadcast = new Broadcast(
                "Только для админов",
                BroadcastCategory.INFO,
                BroadcastAudience.ADMINS,
                "Текст уведомления",
                Instant.now(),
                10,
                BroadcastStatus.SENT
        );
        broadcast.setId(5L);
        when(broadcastRepository.findById(5L)).thenReturn(java.util.Optional.of(broadcast));

        boolean marked = service.markRead(109L, Set.of("USER"), 5L);

        assertThat(marked).isFalse();
        verify(notificationReadRepository, never()).save(any());
    }

    private Broadcast broadcast(long id, String subject) {
        Broadcast broadcast = new Broadcast(
                subject,
                BroadcastCategory.INFO,
                BroadcastAudience.ALL,
                "Текст уведомления",
                Instant.now(),
                10,
                BroadcastStatus.SENT
        );
        broadcast.setId(id);
        return broadcast;
    }
}
