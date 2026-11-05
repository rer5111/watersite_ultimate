package com.waltercraft.notification.service;

import com.waltercraft.notification.dto.BroadcastRequest;
import com.waltercraft.notification.dto.BroadcastResponse;
import com.waltercraft.notification.entity.Broadcast;
import com.waltercraft.notification.entity.BroadcastAudience;
import com.waltercraft.notification.entity.BroadcastCategory;
import com.waltercraft.notification.entity.BroadcastStatus;
import com.waltercraft.notification.repository.BroadcastRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BroadcastServiceTest {

    @Mock
    private BroadcastRepository broadcastRepository;

    private BroadcastService broadcastService;

    @BeforeEach
    void setUp() {
        broadcastService = new BroadcastService(broadcastRepository);
    }

    @Test
    void dispatch_storesSentBroadcastWithRecipients() {
        when(broadcastRepository.save(any(Broadcast.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BroadcastResponse response = broadcastService.dispatch(new BroadcastRequest(
                "Технические работы",
                BroadcastCategory.WARNING,
                BroadcastAudience.ALL,
                "Сервер будет недоступен с 02:00 до 04:00",
                1840
        ));

        assertThat(response.status()).isEqualTo(BroadcastStatus.SENT);
        assertThat(response.recipients()).isEqualTo(1840);
        assertThat(response.sentAt()).isNotNull();
        assertThat(response.subject()).isEqualTo("Технические работы");
    }
}
