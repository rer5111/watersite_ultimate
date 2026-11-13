package com.waltercraft.payment.controller;

import com.waltercraft.payment.client.yoomoney.dto.YoomoneyNotificationPayload;
import com.waltercraft.payment.service.DonationPaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments/webhooks/yoomoney")
public class YoomoneyWebhookController {

    private static final Logger log = LoggerFactory.getLogger(YoomoneyWebhookController.class);

    private final DonationPaymentService donationPaymentService;

    public YoomoneyWebhookController(DonationPaymentService donationPaymentService) {
        this.donationPaymentService = donationPaymentService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> handleWebhook(@RequestParam Map<String, String> params) {
        log.info("Received YooMoney webhook for operation_id={} label={}",
                params.get("operation_id"), params.get("label"));

        YoomoneyNotificationPayload payload = new YoomoneyNotificationPayload(
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
                params.get("unaccepted")
        );

        donationPaymentService.processWebhook(payload);
        return ResponseEntity.ok("OK");
    }
}
