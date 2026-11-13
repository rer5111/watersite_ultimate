package com.waltercraft.payment;

import com.waltercraft.payment.config.PaymentJwtProperties;
import com.waltercraft.payment.config.YoomoneyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({PaymentJwtProperties.class, YoomoneyProperties.class})
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
