package org.example.apigateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@ConditionalOnProperty(name = "GATEWAY_JWT_ENABLED", havingValue = "true")
public class JwtSecurityConfig {

    @Bean
    public SecurityWebFilterChain jwtSecurityWebFilterChain(
            ServerHttpSecurity http,
            @Value("${JWT_JWK_SET_URI:${jwt.jwk-set-uri:}}") String jwkSetUri
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/*/internal/**", "/internal/**").denyAll()
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/*/v3/api-docs", "/api/*/v3/api-docs/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/player-service/api/user/admins").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/skin-service/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/donation-service/api/v1/donation-types").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/event-service/api/v1/events", "/api/event-service/api/v1/events/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/chat-service/api/v1/chat/status").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/chat-service/api/v1/chat/messages").permitAll()
                        .pathMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/api/player-service/**").authenticated()
                        .pathMatchers("/api/application-service/**").authenticated()
                        .pathMatchers("/api/payment-service/**").authenticated()
                        .pathMatchers("/api/donation-service/**").authenticated()
                        .pathMatchers("/api/chat-service/**").authenticated()
                        .pathMatchers("/api/notification-service/**").authenticated()
                        .pathMatchers("/api/auth-service/auth/microsoft/link").authenticated()
                        .pathMatchers("/api/auth-service/auth/microsoft/device-link").authenticated()
                        .pathMatchers("/api/auth-service/auth/change-email/**").authenticated()
                        .pathMatchers("/api/auth-service/auth/change-password/**").authenticated()
                        .anyExchange().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwkSetUri(jwkSetUri))
                )
                .build();
    }
}
