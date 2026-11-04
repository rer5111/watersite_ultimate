package com.waltercraft.chat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "chat.jwt")
public record ChatJwtProperties(URI jwkSetUri, String issuer) {
}
