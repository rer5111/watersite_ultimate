package com.waltercraft.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.microsoft")
public record MicrosoftOAuthProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        String scopes,
        boolean useMojangServices,
        String deviceClientId
) {
    public MicrosoftOAuthProperties {
        if (scopes == null || scopes.isBlank()) {
            scopes = "XboxLive.signin offline_access";
        }
        if (deviceClientId == null || deviceClientId.isBlank()) {
            deviceClientId = "c36a9fb6-4f2a-41ff-90bd-ae7cc92031eb";
        }
    }
}
