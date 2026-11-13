package com.waltercraft.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DonationCreateResponse(
        UUID donationId,
        String actionUrl,
        String receiver,
        String quickpayForm,
        String targets,
        String paymentType,
        BigDecimal sum,
        String label,
        String successUrl
) {
}
