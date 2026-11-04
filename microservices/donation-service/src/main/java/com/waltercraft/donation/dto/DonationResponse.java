package com.waltercraft.donation.dto;

import com.waltercraft.donation.entity.Donation;

import java.math.BigDecimal;
import java.time.Instant;

public record DonationResponse(
        Long id,
        Long playerId,
        Long donationTypeId,
        String donationTypeName,
        BigDecimal amountRub,
        String paymentReference,
        Instant donatedAt,
        long version
) {
    public static DonationResponse from(Donation donation) {
        return new DonationResponse(
                donation.getId(), donation.getPlayerId(), donation.getDonationType().getId(),
                donation.getDonationType().getName(), donation.getAmountRub(), donation.getPaymentReference(),
                donation.getDonatedAt(), donation.getVersion());
    }
}
