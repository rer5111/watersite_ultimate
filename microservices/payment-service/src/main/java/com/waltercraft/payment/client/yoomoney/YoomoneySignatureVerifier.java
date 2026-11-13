package com.waltercraft.payment.client.yoomoney;

import com.waltercraft.payment.config.YoomoneyProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class YoomoneySignatureVerifier {

    private final YoomoneyProperties properties;

    public YoomoneySignatureVerifier(YoomoneyProperties properties) {
        this.properties = properties;
    }

    public boolean verify(
            String notificationType,
            String operationId,
            String amount,
            String currency,
            String datetime,
            String sender,
            String codepro,
            String label,
            String providedSha1Hash
    ) {
        if (providedSha1Hash == null || providedSha1Hash.isBlank()) {
            return false;
        }

        String secret = properties.notificationSecret() == null ? "" : properties.notificationSecret();
        if (secret.isBlank()) {
            return false;
        }

        String stringToSign = String.join("&",
                notificationType == null ? "" : notificationType,
                operationId == null ? "" : operationId,
                amount == null ? "" : amount,
                currency == null ? "" : currency,
                datetime == null ? "" : datetime,
                sender == null ? "" : sender,
                codepro == null ? "" : codepro,
                secret,
                label == null ? "" : label
        );

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(stringToSign.getBytes(StandardCharsets.UTF_8));
            String calculatedHash = HexFormat.of().formatHex(digest);

            return MessageDigest.isEqual(
                    calculatedHash.toLowerCase().getBytes(StandardCharsets.UTF_8),
                    providedSha1Hash.toLowerCase().getBytes(StandardCharsets.UTF_8)
            );
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 algorithm not available", e);
        }
    }
}
