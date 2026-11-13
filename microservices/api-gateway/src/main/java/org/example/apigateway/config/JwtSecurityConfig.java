package org.example.apigateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebFluxSecurity
@ConditionalOnProperty(name = "GATEWAY_JWT_ENABLED", havingValue = "true")
public class JwtSecurityConfig {

    private static final Set<String> ALLOWED_ROLES = Set.of("USER", "ADMIN");

    @Bean
    public SecurityWebFilterChain jwtSecurityWebFilterChain(
            ServerHttpSecurity http,
            @Value("${JWT_JWK_SET_URI:${jwt.jwk-set-uri:}}") String jwkSetUri
    ) {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles == null) {
                return List.of();
            }
            Collection<GrantedAuthority> authorities = roles.stream()
                    .filter(ALLOWED_ROLES::contains)
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
            return authorities;
        });
        Converter<Jwt, Mono<AbstractAuthenticationToken>> reactiveJwtAuthenticationConverter =
                new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/*/internal/**", "/internal/**").denyAll()
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()
                        .pathMatchers("/fallback/**").denyAll()
                        .pathMatchers(HttpMethod.GET, "/api/*/v3/api-docs", "/api/*/v3/api-docs/**").permitAll()
                        .pathMatchers("/api/*/actuator/health/**", "/api/*/actuator/info").permitAll()
                        .pathMatchers("/api/*/actuator/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/player-service/api/user/admins").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/skin-service/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/donation-service/api/v1/donation-types").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/season-service/api/v1/seasons", "/api/season-service/api/v1/seasons/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/event-service/api/v1/events", "/api/event-service/api/v1/events/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/chat-service/api/v1/chat/status").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/chat-service/api/v1/chat/messages").permitAll()
                        .pathMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .pathMatchers("/actuator/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/payment-service/api/v1/payments/webhooks/**").permitAll()
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
                        .jwt(jwt -> jwt.jwkSetUri(jwkSetUri)
                                .jwtAuthenticationConverter(reactiveJwtAuthenticationConverter))
                )
                .build();
    }
}
