package com.waltercraft.auth.service;

import com.nimbusds.jose.jwk.RSAKey;
import com.waltercraft.auth.config.JwtConfiguration;
import com.waltercraft.auth.config.JwtProperties;
import com.waltercraft.auth.config.JwtSigningKey;
import com.waltercraft.auth.dto.AuthenticatedUser;
import com.waltercraft.auth.dto.TokenResponse;
import com.waltercraft.auth.exception.InvalidRefreshTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.time.Clock;
import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenServiceTest {

    private JwtTokenService tokenService;
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                "https://auth.test",
                "",
                true,
                Duration.ofMinutes(15),
                Duration.ofDays(7)
        );
        JwtConfiguration configuration = new JwtConfiguration();
        JwtSigningKey signingKey = configuration.jwtSigningKey(properties);
        RSAKey publicJwk = configuration.jwk(signingKey);
        JwtEncoder jwtEncoder = configuration.jwtEncoder(signingKey);
        jwtDecoder = configuration.jwtDecoder(signingKey, properties);
        tokenService = new JwtTokenService(jwtEncoder, jwtDecoder, properties, signingKey, Clock.systemUTC());
    }

    @Test
    void signsAccessAndRefreshTokensWithSeparateTypes() {
        java.util.UUID uuid = java.util.UUID.randomUUID();
        TokenResponse tokens = tokenService.issueTokens(new AuthenticatedUser(3L, "player", uuid, Set.of("USER")));

        Jwt accessToken = jwtDecoder.decode(tokens.accessToken());
        Jwt refreshToken = jwtDecoder.decode(tokens.refreshToken());
        assertEquals("player", accessToken.getSubject());
        assertEquals(uuid.toString(), accessToken.getClaimAsString("uuid"));
        assertEquals(uuid.toString(), refreshToken.getClaimAsString("uuid"));
        assertEquals("access", accessToken.getClaimAsString("token_type"));
        assertEquals("refresh", refreshToken.getClaimAsString("token_type"));
        assertEquals(Set.of("USER"), Set.copyOf(accessToken.getClaimAsStringList("roles")));
        assertEquals(900, tokens.expiresIn());
    }

    @Test
    void refreshRotatesTokenPairAndRejectsAccessTokens() {
        TokenResponse first = tokenService.issueTokens(new AuthenticatedUser(3L, "player", Set.of("USER")));
        TokenResponse second = tokenService.refresh(first.refreshToken());

        assertEquals("access", jwtDecoder.decode(second.accessToken()).getClaimAsString("token_type"));
        assertEquals("refresh", jwtDecoder.decode(second.refreshToken()).getClaimAsString("token_type"));
        assertThrows(InvalidRefreshTokenException.class, () -> tokenService.refresh(first.accessToken()));
        assertThrows(InvalidRefreshTokenException.class, () -> tokenService.refresh("not-a-jwt"));
    }
}
