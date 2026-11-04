package com.waltercraft.chat.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException exception) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            details.putIfAbsent(error.getField(), error.getDefaultMessage() == null
                    ? "Недопустимое значение" : error.getDefaultMessage());
        }
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Проверьте переданные данные", details);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, ConstraintViolationException.class})
    public ResponseEntity<ProblemDetail> invalidRequest() {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Параметры запроса имеют неверный формат", Map.of());
    }

    @ExceptionHandler(DiscordUnavailableException.class)
    public ResponseEntity<ProblemDetail> discordUnavailable(DiscordUnavailableException exception) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "DISCORD_UNAVAILABLE", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> unexpected(Exception exception) {
        log.error("Unexpected error while handling a chat request", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Внутренняя ошибка сервера", Map.of());
    }

    private static ResponseEntity<ProblemDetail> problem(
            HttpStatus status, String code, String message, Map<String, String> details) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, message);
        problem.setType(URI.create("about:blank"));
        problem.setTitle(status.getReasonPhrase());
        problem.setProperty("code", code);
        problem.setProperty("message", message);
        problem.setProperty("details", details);
        return ResponseEntity.status(status).body(problem);
    }
}
