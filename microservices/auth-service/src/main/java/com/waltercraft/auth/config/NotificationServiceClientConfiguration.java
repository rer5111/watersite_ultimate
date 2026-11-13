package com.waltercraft.auth.config;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class NotificationServiceClientConfiguration {

    @Bean
    public RestClient notificationServiceRestClient(
            @Value("${notification-service.base-url:http://localhost:8087}") String baseUrl,
            @Value("${security.notification-service.shared-secret}") String sharedSecret
    ) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .requestInterceptor((request, body, execution) -> {
                    if (sharedSecret != null && !sharedSecret.isBlank()) {
                        request.getHeaders().set("X-Internal-Auth", sharedSecret);
                    }
                    String requestId = MDC.get("requestId");
                    if (requestId != null && !requestId.isBlank()) {
                        request.getHeaders().set("X-Request-Id", requestId);
                    }
                    return execution.execute(request, body);
                })
                .build();
    }
}
