package com.waltercraft.donation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DonationCreateRequest(
        @NotNull @Positive Long playerId,
        @NotNull @Positive Long donationTypeId,
        @Size(max = 128) String paymentReference
) {
}
