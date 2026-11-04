package com.waltercraft.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "payment.jwt")
public record PaymentJwtProperties(URI jwkSetUri, String issuer) {
}
