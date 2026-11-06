package com.waltercraft.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PlayerServiceClientConfiguration {

    @Bean
    public RestClient playerServiceRestClient(
            @Value("${player-service.base-url:http://localhost:8081}") String baseUrl,
            @Value("${security.auth-service.shared-secret}") String sharedSecret
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
