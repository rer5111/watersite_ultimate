package com.waltercraft.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "auth.jwt")
public record JwtProperties(
        String issuer,
        String privateKeyBase64,
        boolean allowEphemeralKey,
        Duration accessTokenTtl,
        Duration refreshTokenTtl
) {
}
