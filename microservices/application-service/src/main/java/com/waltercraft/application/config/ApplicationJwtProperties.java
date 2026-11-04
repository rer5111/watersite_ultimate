package com.waltercraft.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "application.jwt")
public record ApplicationJwtProperties(URI jwkSetUri, String issuer) {
}
