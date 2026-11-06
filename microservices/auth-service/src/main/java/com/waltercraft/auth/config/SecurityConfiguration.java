package com.waltercraft.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Configuration
public class SecurityConfiguration {

    private static final Set<String> ALLOWED_ROLES = Set.of("USER", "ADMIN");

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register/**", "/auth/resend-code").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/microsoft/authorize-url").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/microsoft/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/microsoft/link").authenticated()
                        .requestMatchers(HttpMethod.POST, "/auth/microsoft/device-code", "/auth/microsoft/device-login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/microsoft/device-link").authenticated()
                        .requestMatchers(HttpMethod.POST, "/auth/change-email/**", "/auth/change-password/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/.well-known/jwks.json").permitAll()
                        .requestMatchers("/actuator/metrics/**", "/actuator/prometheus").hasRole("ADMIN")
                        .requestMatchers("/actuator/health/**", "/actuator/info",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()
                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                        .decoder(jwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    private Converter<Jwt, ? extends org.springframework.security.authentication.AbstractAuthenticationToken>
    jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
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
        return converter;
    }
}
