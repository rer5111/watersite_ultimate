package com.waltercraft.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class NotificationServiceClientConfiguration {

    @Bean
    public RestClient notificationServiceRestClient(
            @Value("${notification-service.base-url:http://localhost:8087}") String baseUrl,
            @Value("${security.notification-service.shared-secret}") String sharedSecret
    ) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor((request, body, execution) -> {
                    if (sharedSecret != null && !sharedSecret.isBlank()) {
                        request.getHeaders().set("X-Internal-Auth", sharedSecret);
                    }
                    return execution.execute(request, body);
                })
                .build();
    }
}
