package org.example.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@ConditionalOnProperty(name = "GATEWAY_JWT_ENABLED", havingValue = "false", matchIfMissing = true)
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf().disable()
                .authorizeExchange()
                .pathMatchers(HttpMethod.GET, "/api/player-service/api/user/admins").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/skin-service/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/donation-service/api/v1/donation-types").permitAll()
                .pathMatchers("/actuator/health/**", "/actuator/info").permitAll()
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .pathMatchers("/api/player-service/**").denyAll()
                .pathMatchers("/api/application-service/**").denyAll()
                .pathMatchers("/api/payment-service/**").denyAll()
                .pathMatchers("/api/donation-service/**").denyAll()
                .anyExchange().permitAll()
                .and()
                .build();
    }
}
