package com.waltercraft.donation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DonationTypeCreateRequest(
        @NotBlank
        @Size(max = 64)
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$")
        String code,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal priceRub,
        @PositiveOrZero int displayOrder,
        Boolean active
) {
}
