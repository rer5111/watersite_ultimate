package org.example.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${JWT_JWK_SET_URI:${security.oauth2.resourceserver.jwt.jwk-set-uri:http://localhost:8090/.well-known/jwks.json}}") String jwkSetUri
    ) throws Exception {
        return http.csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/user/admins").permitAll()
                // Internal authentication calls are protected by the shared X-Internal-Auth secret.
                .antMatchers("/internal/auth/**").permitAll()
                .antMatchers("/actuator/health/**", "/actuator/info").permitAll()
                .anyRequest().authenticated()
                .and()
                .httpBasic()
                .and()
                .oauth2ResourceServer()
                .jwt()
                .jwkSetUri(jwkSetUri)
                .and()
                .and()
                .build();
    }
}
