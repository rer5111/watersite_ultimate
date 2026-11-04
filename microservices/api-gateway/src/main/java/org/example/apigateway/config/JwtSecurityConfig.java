package org.example.apigateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@ConditionalOnProperty(name = "GATEWAY_JWT_ENABLED", havingValue = "true")
public class JwtSecurityConfig {

    @Bean
    public SecurityWebFilterChain jwtSecurityWebFilterChain(
            ServerHttpSecurity http,
            @Value("${JWT_JWK_SET_URI:${jwt.jwk-set-uri:}}") String jwkSetUri
    ) {
        return http
                .csrf().disable()
                .authorizeExchange()
                .pathMatchers(HttpMethod.GET, "/api/player-service/api/user/admins").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/skin-service/**").permitAll()
                .pathMatchers("/actuator/health/**", "/actuator/info").permitAll()
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .pathMatchers("/api/player-service/**").authenticated()
                .anyExchange().permitAll()
                .and()
                .oauth2ResourceServer()
                .jwt().jwkSetUri(jwkSetUri)
                .and()
                .and()
                .build();
    }
}
