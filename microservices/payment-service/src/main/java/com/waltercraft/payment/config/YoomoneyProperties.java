package com.waltercraft.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yoomoney")
public record YoomoneyProperties(
        String wallet,
        String notificationSecret,
        String successUrl,
        String quickpayUrl
) {
}
