package com.waltercraft.notification.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RateLimitExceededException.class)
    public ProblemDetail handleRateLimitExceeded(RateLimitExceededException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
        problem.setTitle("Превышен лимит запросов");
        problem.setType(URI.create("urn:problem:rate-limit-exceeded"));
        problem.setProperty("retryAfterSeconds", ex.getRetryAfterSeconds());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Ошибка валидации входных данных");
        problem.setTitle("Некорректные параметры");
        problem.setType(URI.create("urn:problem:validation-error"));

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        problem.setProperty("errors", fieldErrors);
        return problem;
    }

    @ExceptionHandler(EmailDeliveryUnavailableException.class)
    public ProblemDetail handleEmailDeliveryUnavailable(EmailDeliveryUnavailableException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Отправка письма временно недоступна. Повторите попытку позже.");
        problem.setTitle("Сервис почты недоступен");
        problem.setType(URI.create("urn:problem:email-delivery-unavailable"));
        problem.setProperty("code", "EMAIL_DELIVERY_UNAVAILABLE");
        problem.setProperty("message", problem.getDetail());
        problem.setProperty("details", Map.of());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unhandled notification-service request failure", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Произошла внутренняя ошибка сервера");
        problem.setTitle("Внутренняя ошибка");
        problem.setType(URI.create("urn:problem:internal-server-error"));
        return problem;
    }
}
