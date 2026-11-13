package com.waltercraft.payment.service;

import com.waltercraft.payment.client.notification.NotificationServiceClient;
import com.waltercraft.payment.client.yoomoney.YoomoneySignatureVerifier;
import com.waltercraft.payment.client.yoomoney.dto.YoomoneyNotificationPayload;
import com.waltercraft.payment.config.YoomoneyProperties;
import com.waltercraft.payment.dto.DonationCreateRequest;
import com.waltercraft.payment.dto.DonationCreateResponse;
import com.waltercraft.payment.dto.DonationStatusResponse;
import com.waltercraft.payment.entity.Donation;
import com.waltercraft.payment.entity.DonationStatus;
import com.waltercraft.payment.exception.ResourceNotFoundException;
import com.waltercraft.payment.repository.DonationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DonationPaymentService {

    private static final Logger log = LoggerFactory.getLogger(DonationPaymentService.class);
    private static final String DEFAULT_YOOMONEY_QUICKPAY_URL = "https://yoomoney.ru/quickpay/confirm.xml";

    private final DonationRepository donationRepository;
    private final YoomoneyProperties properties;
    private final YoomoneySignatureVerifier signatureVerifier;
    private final NotificationServiceClient notificationServiceClient;

    public DonationPaymentService(
            DonationRepository donationRepository,
            YoomoneyProperties properties,
            YoomoneySignatureVerifier signatureVerifier,
            NotificationServiceClient notificationServiceClient
    ) {
        this.donationRepository = donationRepository;
        this.properties = properties;
        this.signatureVerifier = signatureVerifier;
        this.notificationServiceClient = notificationServiceClient;
    }

    @Transactional
    public DonationCreateResponse createDonation(Long userId, String username, DonationCreateRequest request) {
        Donation donation = new Donation(
                userId,
                username,
                request.amount().setScale(2, RoundingMode.HALF_UP),
                request.message()
        );
        Donation saved = donationRepository.save(donation);

        String baseSuccessUrl = properties.successUrl() != null && !properties.successUrl().isBlank()
                ? properties.successUrl()
                : "/donations/result";
        String successUrl = baseSuccessUrl.contains("?")
                ? baseSuccessUrl + "&donationId=" + saved.getId()
                : baseSuccessUrl + "?donationId=" + saved.getId();

        String actionUrl = properties.quickpayUrl() != null && !properties.quickpayUrl().isBlank()
                ? properties.quickpayUrl()
                : DEFAULT_YOOMONEY_QUICKPAY_URL;

        log.info("Donation created donationId={} userId={} amount={}",
                saved.getId(), userId, saved.getAmountRub());

        return new DonationCreateResponse(
                saved.getId(),
                actionUrl,
                properties.wallet(),
                "donate",
                "Добровольное пожертвование Waltercraft",
                "AC",
                saved.getAmountRub(),
                saved.getId().toString(),
                successUrl
        );
    }

    @Transactional
    public void processWebhook(YoomoneyNotificationPayload payload) {
        if (payload == null || payload.label() == null || payload.label().isBlank()) {
            log.warn("Received empty or invalid YooMoney webhook payload");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректное уведомление");
        }

        boolean validSignature = signatureVerifier.verify(
                payload.notification_type(),
                payload.operation_id(),
                payload.amount(),
                payload.currency(),
                payload.datetime(),
                payload.sender(),
                payload.codepro(),
                payload.label(),
                payload.sha1_hash()
        );

        if (!validSignature) {
            log.warn("Invalid SHA-1 signature for YooMoney operation_id={} label={}",
                    payload.operation_id(), payload.label());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверная цифровая подпись");
        }

        if ("true".equalsIgnoreCase(payload.unaccepted())) {
            log.info("Payment operation_id={} is unaccepted/held, waiting for acceptance",
                    payload.operation_id());
            return;
        }

        UUID donationId;
        try {
            donationId = UUID.fromString(payload.label());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid donation UUID label: {}", payload.label());
            return;
        }

        Optional<Donation> donationOpt = donationRepository.findById(donationId);
        if (donationOpt.isEmpty()) {
            log.warn("Donation not found for label={}", payload.label());
            return;
        }

        Donation donation = donationOpt.get();
        if (donation.getStatus() != DonationStatus.PENDING) {
            log.info("Donation donationId={} is already in status={}, skipping",
                    donation.getId(), donation.getStatus());
            return;
        }

        if (payload.withdraw_amount() != null && !payload.withdraw_amount().isBlank()) {
            try {
                donation.setAmountRub(new BigDecimal(payload.withdraw_amount()).setScale(2, RoundingMode.HALF_UP));
            } catch (NumberFormatException ignored) {
            }
        } else if (payload.amount() != null && !payload.amount().isBlank()) {
            try {
                donation.setAmountRub(new BigDecimal(payload.amount()).setScale(2, RoundingMode.HALF_UP));
            } catch (NumberFormatException ignored) {
            }
        }

        donation.setStatus(DonationStatus.SUCCEEDED);
        donation.setPaymentReference(payload.operation_id());
        donationRepository.save(donation);

        log.info("Donation donationId={} marked as SUCCEEDED via YooMoney operation_id={}",
                donation.getId(), payload.operation_id());

        notificationServiceClient.sendUserNotification(
                donation.getUserId(),
                "Пожертвование получено",
                "SUCCESS",
                "Спасибо за поддержку сервера Waltercraft! Ваше пожертвование на сумму " + donation.getAmountRub() + " ₽ успешно зачислено."
        );
    }

    public DonationStatusResponse getDonation(UUID id, Long currentUserId, boolean isAdmin) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пожертвование не найдено"));

        if (!isAdmin && (currentUserId == null || !currentUserId.equals(donation.getUserId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав для просмотра этого пожертвования");
        }

        return DonationStatusResponse.from(donation);
    }
}
