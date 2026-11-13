package com.waltercraft.payment.client.yoomoney.dto;

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
        String unaccepted
) {
}
