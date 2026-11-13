package com.waltercraft.payment.client.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
public class NotificationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceClient.class);
    private static final String INTERNAL_AUTH_HEADER = "X-Internal-Auth";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final RestClient restClient;
    private final String sharedSecret;

    public NotificationServiceClient(
            @Value("${notification-service.base-url:http://localhost:8087}") String baseUrl,
            @Value("${notification-service.shared-secret:}") String sharedSecret
    ) {
        this.sharedSecret = sharedSecret == null ? "" : sharedSecret;

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .requestInterceptor((request, body, execution) -> {
                    String requestId = MDC.get("requestId");
                    if (requestId != null && !requestId.isBlank()) {
                        request.getHeaders().set(REQUEST_ID_HEADER, requestId);
                    }
                    return execution.execute(request, body);
                })
                .build();
    }

    public void sendUserNotification(Long userId, String subject, String category, String message) {
        if (userId == null) {
            return;
        }

        try {
            record Request(Long userId, String subject, String category, String message) {}

            restClient.post()
                    .uri("/internal/notifications/user")
                    .header(INTERNAL_AUTH_HEADER, sharedSecret)
                    .body(new Request(userId, subject, category, message))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Successfully delivered in-app notification to user={}", userId);
        } catch (Exception ex) {
            log.error("Failed to deliver notification to user={}", userId, ex);
        }
    }
}
