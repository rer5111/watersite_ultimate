package com.waltercraft.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

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
                        .requestMatchers(HttpMethod.GET, "/.well-known/jwks.json").permitAll()
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder)))
                .build();
    }
}
