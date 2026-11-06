package org.example.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Set<String> ALLOWED_ROLES = Set.of("USER", "ADMIN");

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
                .antMatchers("/actuator/metrics/**", "/actuator/prometheus").hasRole("ADMIN")
                .antMatchers("/actuator/health/**", "/actuator/info",
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()
                // Current user endpoints accessible by any authenticated user
                .antMatchers("/api/user/me", "/api/user/me/**").authenticated()
                .antMatchers(HttpMethod.GET, "/api/season/me").authenticated()
                .antMatchers(HttpMethod.POST, "/api/user/role-requests").authenticated()
                // Admin management endpoints require ADMIN role
                .antMatchers("/api/user/**", "/api/account/**", "/api/season/**").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and()
                .httpBasic()
                .and()
                .oauth2ResourceServer()
                .jwt()
                .jwkSetUri(jwkSetUri)
                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                .and()
                .and()
                .build();
    }

    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
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
