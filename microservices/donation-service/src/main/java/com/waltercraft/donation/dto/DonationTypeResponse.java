package com.waltercraft.donation.dto;

import com.waltercraft.donation.entity.DonationType;

import java.math.BigDecimal;
import java.time.Instant;

public record DonationTypeResponse(
        Long id,
        String code,
        String name,
        String description,
        BigDecimal priceRub,
        int displayOrder,
        boolean active,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
    public static DonationTypeResponse from(DonationType donationType) {
        return new DonationTypeResponse(
                donationType.getId(), donationType.getCode(), donationType.getName(), donationType.getDescription(),
                donationType.getPriceRub(), donationType.getDisplayOrder(), donationType.isActive(),
                donationType.getVersion(), donationType.getCreatedAt(), donationType.getUpdatedAt());
    }
}
