package com.waltercraft.auth.service;

import com.waltercraft.auth.dto.InternalSendCodeRequest;
import com.waltercraft.auth.dto.InternalVerifyCodeRequest;
import com.waltercraft.auth.dto.InternalVerifyCodeResponse;
import com.waltercraft.auth.dto.SendCodeResponse;
import com.waltercraft.auth.exception.InvalidVerificationCodeException;
import com.waltercraft.auth.exception.NotificationServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class NotificationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceClient.class);

    private final RestClient notificationRestClient;

    public NotificationServiceClient(@Qualifier("notificationServiceRestClient") RestClient notificationRestClient) {
        this.notificationRestClient = notificationRestClient;
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendCodeFallback")
    public SendCodeResponse sendCode(String email, String purpose) {
        try {
            return notificationRestClient.post()
                    .uri("/internal/notifications/email/send-code")
                    .body(new InternalSendCodeRequest(email, purpose))
                    .retrieve()
                    .body(SendCodeResponse.class);
        } catch (RestClientException ex) {
            throw new NotificationServiceUnavailableException(ex);
        }
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "verifyCodeFallback")
    public void verifyCode(String email, String code, String purpose) {
        InternalVerifyCodeResponse response;
        try {
            response = notificationRestClient.post()
                    .uri("/internal/notifications/email/verify-code")
                    .body(new InternalVerifyCodeRequest(email, code, purpose))
                    .retrieve()
                    .body(InternalVerifyCodeResponse.class);
        } catch (RestClientException ex) {
            throw new NotificationServiceUnavailableException(ex);
        }

        if (response == null || !response.verified()) {
            String message = response != null && response.message() != null
                    ? response.message()
                    : "Неверный код подтверждения";
            throw new InvalidVerificationCodeException(message);
        }
    }

    private SendCodeResponse sendCodeFallback(String email, String purpose, Throwable cause) {
        throw fallbackFailure(cause);
    }

    private void verifyCodeFallback(String email, String code, String purpose, Throwable cause) {
        throw fallbackFailure(cause);
    }

    private RuntimeException fallbackFailure(Throwable cause) {
        if (cause instanceof InvalidVerificationCodeException invalidCode) {
            return invalidCode;
        }
        if (cause instanceof NotificationServiceUnavailableException unavailable) {
            log.warn("Notification service fallback invoked exceptionType={}", cause.getClass().getSimpleName());
            return unavailable;
        }
        log.warn("Notification service fallback invoked exceptionType={}", cause.getClass().getSimpleName());
        return new NotificationServiceUnavailableException(cause);
    }
}
