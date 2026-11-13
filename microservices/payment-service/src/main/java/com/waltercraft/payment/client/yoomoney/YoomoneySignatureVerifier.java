package com.waltercraft.payment.client.yoomoney;

import com.waltercraft.payment.config.YoomoneyProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Component
public class YoomoneySignatureVerifier {

    private static final Logger log = LoggerFactory.getLogger(YoomoneySignatureVerifier.class);

    private final YoomoneyProperties properties;

    public YoomoneySignatureVerifier(YoomoneyProperties properties) {
        this.properties = properties;
    }

    public boolean verify(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return false;
        }

        String secret = properties.notificationSecret() == null ? "" : properties.notificationSecret().trim();
        if (secret.isBlank()) {
            log.warn("YooMoney notification secret is blank or unconfigured");
            return false;
        }

        String sign = params.get("sign");
        if (sign != null && !sign.isBlank()) {
            boolean validHmac = verifyHmacSha256(params, secret, sign);
            if (validHmac) {
                return true;
            }
            log.debug("HMAC-SHA256 sign verification failed, checking sha1_hash fallback if present");
        }

        String sha1Hash = params.get("sha1_hash");
        if (sha1Hash != null && !sha1Hash.isBlank()) {
            return verifyLegacySha1(params, secret, sha1Hash);
        }

        return false;
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
        Map<String, String> params = new HashMap<>();
        if (notificationType != null) params.put("notification_type", notificationType);
        if (operationId != null) params.put("operation_id", operationId);
        if (amount != null) params.put("amount", amount);
        if (currency != null) params.put("currency", currency);
        if (datetime != null) params.put("datetime", datetime);
        if (sender != null) params.put("sender", sender);
        if (codepro != null) params.put("codepro", codepro);
        if (label != null) params.put("label", label);
        if (providedSha1Hash != null) params.put("sha1_hash", providedSha1Hash);
        return verify(params);
    }

    private boolean verifyHmacSha256(Map<String, String> params, String secret, String expectedSign) {
        List<String> sortedKeys = params.keySet().stream()
                .filter(key -> !"sign".equals(key))
                .sorted()
                .toList();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sortedKeys.size(); i++) {
            if (i > 0) {
                sb.append("&");
            }
            String key = sortedKeys.get(i);
            String val = params.get(key);
            sb.append(key).append("=").append(encodeRfc3986(val));
        }

        String stringToSign = sb.toString();
        log.debug("HMAC stringToSign: {}", stringToSign);

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String calculatedSign = HexFormat.of().formatHex(hmacBytes);

            return MessageDigest.isEqual(
                    calculatedSign.toLowerCase().getBytes(StandardCharsets.UTF_8),
                    expectedSign.trim().toLowerCase().getBytes(StandardCharsets.UTF_8)
            );
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to calculate HMAC-SHA256 for YooMoney notification", e);
        }
    }

    private boolean verifyLegacySha1(Map<String, String> params, String secret, String expectedHash) {
        String stringToSign = String.join("&",
                params.getOrDefault("notification_type", ""),
                params.getOrDefault("operation_id", ""),
                params.getOrDefault("amount", ""),
                params.getOrDefault("currency", ""),
                params.getOrDefault("datetime", ""),
                params.getOrDefault("sender", ""),
                params.getOrDefault("codepro", ""),
                secret,
                params.getOrDefault("label", "")
        );

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(stringToSign.getBytes(StandardCharsets.UTF_8));
            String calculatedHash = HexFormat.of().formatHex(digest);

            return MessageDigest.isEqual(
                    calculatedHash.toLowerCase().getBytes(StandardCharsets.UTF_8),
                    expectedHash.trim().toLowerCase().getBytes(StandardCharsets.UTF_8)
            );
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 algorithm not available", e);
        }
    }

    public static String encodeRfc3986(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }
}
