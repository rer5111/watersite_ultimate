package com.waltercraft.auth.service;

import com.waltercraft.auth.dto.InternalSendCodeRequest;
import com.waltercraft.auth.dto.InternalVerifyCodeRequest;
import com.waltercraft.auth.dto.InternalVerifyCodeResponse;
import com.waltercraft.auth.dto.SendCodeResponse;
import com.waltercraft.auth.exception.InvalidVerificationCodeException;
import com.waltercraft.auth.exception.NotificationServiceUnavailableException;
import com.waltercraft.auth.exception.RateLimitExceededException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

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
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                int retryAfter = 60;
                String message = "Подождите перед повторной отправкой кода";
                try {
                    ProblemDetail problem = ex.getResponseBodyAs(ProblemDetail.class);
                    if (problem != null) {
                        if (problem.getDetail() != null && !problem.getDetail().isBlank()) {
                            message = problem.getDetail();
                        }
                        Object retryProp = problem.getProperties() != null
                                ? problem.getProperties().get("retryAfterSeconds")
                                : null;
                        if (retryProp instanceof Number number) {
                            retryAfter = number.intValue();
                        }
                    }
                } catch (Exception ignored) {
                    // keep default message and retryAfter
                }
                throw new RateLimitExceededException(message, retryAfter);
            }
            throw new NotificationServiceUnavailableException(ex);
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
        if (cause instanceof RateLimitExceededException rateLimit) {
            return rateLimit;
        }
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
