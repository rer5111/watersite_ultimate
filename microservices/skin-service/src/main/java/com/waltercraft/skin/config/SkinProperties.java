package com.waltercraft.skin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "skin.provider")
public record SkinProperties(
        String vzgeUrl,
        String crafatarUrl,
        String visageUrl,
        String userAgent,
        int cacheTtlMinutes,
        int connectTimeoutMs,
        int readTimeoutMs
) {
    public SkinProperties {
        if (vzgeUrl == null || vzgeUrl.isBlank()) {
            vzgeUrl = "https://vzge.me";
        }
        if (crafatarUrl == null || crafatarUrl.isBlank()) {
            crafatarUrl = "https://crafatar.com";
        }
        if (visageUrl == null || visageUrl.isBlank()) {
            visageUrl = "https://visage.surgeplay.com";
        }
        if (userAgent == null || userAgent.isBlank()) {
            userAgent = "WaltercraftSkinService/1.0 (+https://waltercraft.ru; admin@waltercraft.ru)";
        }
        if (cacheTtlMinutes <= 0) {
            cacheTtlMinutes = 1440;
        }
        if (connectTimeoutMs <= 0) {
            connectTimeoutMs = 3000;
        }
        if (readTimeoutMs <= 0) {
            readTimeoutMs = 5000;
        }
    }
}
