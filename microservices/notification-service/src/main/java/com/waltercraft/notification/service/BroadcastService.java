package com.waltercraft.notification.service;

import com.waltercraft.notification.dto.BroadcastRequest;
import com.waltercraft.notification.dto.BroadcastResponse;
import com.waltercraft.notification.entity.Broadcast;
import com.waltercraft.notification.entity.BroadcastStatus;
import com.waltercraft.notification.repository.BroadcastRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BroadcastService {

    private static final Logger log = LoggerFactory.getLogger(BroadcastService.class);

    private final BroadcastRepository broadcastRepository;

    public BroadcastService(BroadcastRepository broadcastRepository) {
        this.broadcastRepository = broadcastRepository;
    }

    public List<BroadcastResponse> listHistory() {
        return broadcastRepository.findAllByOrderBySentAtDesc().stream()
                .map(BroadcastResponse::from)
                .toList();
    }

    @Transactional
    public BroadcastResponse dispatch(BroadcastRequest request) {
        Broadcast broadcast = new Broadcast(
                request.subject().trim(),
                request.category(),
                request.audience(),
                request.message().trim(),
                Instant.now(),
                request.recipients(),
                BroadcastStatus.SENT
        );
        Broadcast saved = broadcastRepository.save(broadcast);
        log.info("Notification broadcast created broadcastId={} audience={}", saved.getId(), saved.getAudience());
        return BroadcastResponse.from(saved);
    }
}
