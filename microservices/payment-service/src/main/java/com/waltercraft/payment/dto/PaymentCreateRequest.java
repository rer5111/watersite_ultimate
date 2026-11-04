package com.waltercraft.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentCreateRequest(
        @NotBlank @Size(max = 120) String payerUsername,
        @NotNull @PastOrPresent LocalDate paymentDate,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amountRub,
        @Size(max = 128) String externalReference
) {
}
