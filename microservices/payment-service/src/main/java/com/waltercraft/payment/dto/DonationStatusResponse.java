package com.waltercraft.payment.dto;

import com.waltercraft.payment.entity.Donation;
import com.waltercraft.payment.entity.DonationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DonationStatusResponse(
        UUID id,
        Long userId,
        String payerUsername,
        BigDecimal amountRub,
        String currency,
        DonationStatus status,
        String message,
        Instant createdAt
) {
    public static DonationStatusResponse from(Donation donation) {
        return new DonationStatusResponse(
                donation.getId(),
                donation.getUserId(),
                donation.getPayerUsername(),
                donation.getAmountRub(),
                donation.getCurrency(),
                donation.getStatus(),
                donation.getMessage(),
                donation.getCreatedAt()
        );
    }
}
