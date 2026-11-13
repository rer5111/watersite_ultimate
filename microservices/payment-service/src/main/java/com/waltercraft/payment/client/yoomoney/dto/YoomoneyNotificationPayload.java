package com.waltercraft.payment.client.yoomoney.dto;

import java.util.Collections;
import java.util.Map;

public record YoomoneyNotificationPayload(
        String notification_type,
        String operation_id,
        String amount,
        String withdraw_amount,
        String currency,
        String datetime,
        String sender,
        String codepro,
        String label,
        String sha1_hash,
        String sign,
        String test_notification,
        String unaccepted,
        Map<String, String> rawParams
) {
    public YoomoneyNotificationPayload(
            String notification_type,
            String operation_id,
            String amount,
            String withdraw_amount,
            String currency,
            String datetime,
            String sender,
            String codepro,
            String label,
            String sha1_hash,
            String unaccepted
    ) {
        this(
                notification_type,
                operation_id,
                amount,
                withdraw_amount,
                currency,
                datetime,
                sender,
                codepro,
                label,
                sha1_hash,
                null,
                null,
                unaccepted,
                Collections.emptyMap()
        );
    }

    public static YoomoneyNotificationPayload from(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return new YoomoneyNotificationPayload(
                    null, null, null, null, null, null, null, null, null, null, null, null, null, Collections.emptyMap()
            );
        }
        return new YoomoneyNotificationPayload(
                params.get("notification_type"),
                params.get("operation_id"),
                params.get("amount"),
                params.get("withdraw_amount"),
                params.get("currency"),
                params.get("datetime"),
                params.get("sender"),
                params.get("codepro"),
                params.get("label"),
                params.get("sha1_hash"),
                params.get("sign"),
                params.get("test_notification"),
                params.get("unaccepted"),
                Map.copyOf(params)
        );
    }

    public boolean isTest() {
        return "true".equalsIgnoreCase(test_notification) || "test-notification".equals(operation_id);
    }
}
