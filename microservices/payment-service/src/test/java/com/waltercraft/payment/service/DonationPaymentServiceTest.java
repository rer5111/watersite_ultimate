package com.waltercraft.payment.service;

import com.waltercraft.payment.client.notification.NotificationServiceClient;
import com.waltercraft.payment.client.yoomoney.YoomoneySignatureVerifier;
import com.waltercraft.payment.client.yoomoney.dto.YoomoneyNotificationPayload;
import com.waltercraft.payment.config.YoomoneyProperties;
import com.waltercraft.payment.dto.DonationCreateRequest;
import com.waltercraft.payment.dto.DonationCreateResponse;
import com.waltercraft.payment.entity.Donation;
import com.waltercraft.payment.entity.DonationStatus;
import com.waltercraft.payment.repository.DonationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DonationPaymentServiceTest {

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private YoomoneySignatureVerifier signatureVerifier;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    private DonationPaymentService service;

    @BeforeEach
    void setUp() {
        YoomoneyProperties properties = new YoomoneyProperties(
                "410011234567890",
                "test-secret",
                "http://localhost:5173/donations/result",
                "https://yoomoney.ru/quickpay/confirm.xml"
        );
        service = new DonationPaymentService(
                donationRepository,
                properties,
                signatureVerifier,
                notificationServiceClient
        );
    }

    @Test
    void createDonationSavesPendingAndReturnsYoomoneyParams() {
        when(donationRepository.save(any(Donation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DonationCreateRequest request = new DonationCreateRequest(BigDecimal.valueOf(250.00), "Тест");
        DonationCreateResponse response = service.createDonation(42L, "steve", request);

        assertNotNull(response.donationId());
        assertEquals("410011234567890", response.receiver());
        assertEquals("https://yoomoney.ru/quickpay/confirm.xml", response.actionUrl());
        assertEquals("donate", response.quickpayForm());
        assertEquals("AC", response.paymentType());
        assertEquals(BigDecimal.valueOf(250.00).setScale(2), response.sum());
        verify(donationRepository).save(any(Donation.class));
    }

    @Test
    void processWebhookValidSignatureUpdatesStatusAndSendsNotification() {
        UUID donationId = UUID.randomUUID();
        Donation donation = new Donation(42L, "steve", BigDecimal.valueOf(300.00), "На сервер");

        when(signatureVerifier.verify(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);
        when(donationRepository.findById(donationId))
                .thenReturn(Optional.of(donation));

        YoomoneyNotificationPayload payload = new YoomoneyNotificationPayload(
                "p2p-incoming",
                "123456789",
                "291.00",
                "300.00",
                "643",
                "2026-11-13T17:00:00Z",
                "",
                "false",
                donationId.toString(),
                "dummy_hash",
                "false"
        );

        service.processWebhook(payload);

        assertEquals(DonationStatus.SUCCEEDED, donation.getStatus());
        assertEquals("123456789", donation.getPaymentReference());
        assertEquals(new BigDecimal("300.00"), donation.getAmountRub());
        verify(notificationServiceClient).sendUserNotification(
                eq(42L),
                anyString(),
                eq("SUCCESS"),
                anyString()
        );
    }

    @Test
    void processWebhookInvalidSignatureThrowsBadRequest() {
        when(signatureVerifier.verify(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(false);

        YoomoneyNotificationPayload payload = new YoomoneyNotificationPayload(
                "p2p-incoming",
                "123456789",
                "291.00",
                "300.00",
                "643",
                "2026-11-13T17:00:00Z",
                "",
                "false",
                UUID.randomUUID().toString(),
                "bad_hash",
                "false"
        );

        assertThrows(ResponseStatusException.class, () -> service.processWebhook(payload));
        verify(donationRepository, never()).save(any());
    }
}
