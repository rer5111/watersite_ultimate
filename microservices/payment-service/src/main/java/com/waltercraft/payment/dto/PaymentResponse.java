package com.waltercraft.payment.dto;

import com.waltercraft.payment.entity.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PaymentResponse(
        Long id,
        String payerUsername,
        LocalDate paymentDate,
        BigDecimal amountRub,
        String externalReference,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPayerUsername(),
                payment.getPaymentDate(),
                payment.getAmountRub(),
                payment.getExternalReference(),
                payment.getCreatedAt(),
                payment.getUpdatedAt(),
                payment.getVersion());
    }
}
