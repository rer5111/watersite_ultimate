package org.example.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@ConditionalOnProperty(name = "GATEWAY_JWT_ENABLED", havingValue = "false", matchIfMissing = true)
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/*/internal/**", "/internal/**").denyAll()
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()
                        .pathMatchers("/fallback/**").denyAll()
                        .pathMatchers(HttpMethod.GET, "/api/*/v3/api-docs", "/api/*/v3/api-docs/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/player-service/api/user/admins").permitAll()
                        .pathMatchers("/api/player-service/api/user/me", "/api/player-service/api/user/me/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/skin-service/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/donation-service/api/v1/donation-types").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/event-service/api/v1/events", "/api/event-service/api/v1/events/**").permitAll()
                        .pathMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .pathMatchers("/actuator/**").denyAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/api/player-service/**").denyAll()
                        .pathMatchers("/api/application-service/**").denyAll()
                        .pathMatchers("/api/payment-service/**").denyAll()
                        .pathMatchers("/api/donation-service/**").denyAll()
                        .pathMatchers("/api/chat-service/**").denyAll()
                        .anyExchange().permitAll()
                )
                .build();
    }
}
