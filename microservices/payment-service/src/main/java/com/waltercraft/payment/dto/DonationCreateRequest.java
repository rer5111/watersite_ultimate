package com.waltercraft.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DonationCreateRequest(
        @NotNull(message = "Сумма пожертвования обязательна")
        @DecimalMin(value = "10.00", message = "Минимальная сумма пожертвования 10.00 RUB")
        BigDecimal amount,

        @Size(max = 500, message = "Сообщение не должно превышать 500 символов")
        String message
) {
}
