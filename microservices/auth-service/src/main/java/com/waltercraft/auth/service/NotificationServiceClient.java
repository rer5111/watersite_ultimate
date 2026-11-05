package com.waltercraft.auth.service;

import com.waltercraft.auth.dto.InternalSendCodeRequest;
import com.waltercraft.auth.dto.InternalVerifyCodeRequest;
import com.waltercraft.auth.dto.InternalVerifyCodeResponse;
import com.waltercraft.auth.dto.SendCodeResponse;
import com.waltercraft.auth.exception.InvalidVerificationCodeException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class NotificationServiceClient {

    private final RestClient notificationRestClient;

    public NotificationServiceClient(@Qualifier("notificationServiceRestClient") RestClient notificationRestClient) {
        this.notificationRestClient = notificationRestClient;
    }

    public SendCodeResponse sendCode(String email, String purpose) {
        try {
            return notificationRestClient.post()
                    .uri("/internal/notifications/email/send-code")
                    .body(new InternalSendCodeRequest(email, purpose))
                    .retrieve()
                    .body(SendCodeResponse.class);
        } catch (RestClientException ex) {
            throw new RuntimeException("Не удалось отправить код подтверждения: " + ex.getMessage(), ex);
        }
    }

    public void verifyCode(String email, String code, String purpose) {
        InternalVerifyCodeResponse response;
        try {
            response = notificationRestClient.post()
                    .uri("/internal/notifications/email/verify-code")
                    .body(new InternalVerifyCodeRequest(email, code, purpose))
                    .retrieve()
                    .body(InternalVerifyCodeResponse.class);
        } catch (RestClientException ex) {
            throw new RuntimeException("Ошибка проверки кода: " + ex.getMessage(), ex);
        }

        if (response == null || !response.verified()) {
            String message = response != null && response.message() != null
                    ? response.message()
                    : "Неверный код подтверждения";
            throw new InvalidVerificationCodeException(message);
        }
    }
}
