package com.waltercraft.auth.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ProblemDetail> invalidCredentials() {
        return problem(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "Неверный логин или пароль", Map.of());
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ProblemDetail> invalidRefreshToken() {
        return problem(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Токен обновления недействителен или истёк", Map.of());
    }

    @ExceptionHandler(PlayerServiceUnavailableException.class)
    public ResponseEntity<ProblemDetail> playerServiceUnavailable() {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "AUTH_DEPENDENCY_UNAVAILABLE", "Сервис проверки аккаунтов временно недоступен", Map.of());
    }

    @ExceptionHandler(MinecraftOAuthException.class)
    public ResponseEntity<ProblemDetail> minecraftOAuthFailed(MinecraftOAuthException exception) {
        return problem(HttpStatus.BAD_REQUEST, "MICROSOFT_AUTH_FAILED", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(MinecraftProfileNotFoundException.class)
    public ResponseEntity<ProblemDetail> minecraftProfileNotFound(MinecraftProfileNotFoundException exception) {
        return problem(HttpStatus.FORBIDDEN, "MINECRAFT_LICENSE_REQUIRED", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(MinecraftAccountAlreadyLinkedException.class)
    public ResponseEntity<ProblemDetail> minecraftAlreadyLinked(MinecraftAccountAlreadyLinkedException exception) {
        return problem(HttpStatus.CONFLICT, "MINECRAFT_ACCOUNT_ALREADY_LINKED", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> userAlreadyExists(UserAlreadyExistsException exception) {
        String field = exception.getField();
        if (field == null && exception.getMessage() != null) {
            String lower = exception.getMessage().toLowerCase();
            if (lower.contains("никнейм") || lower.contains("username")) {
                field = "nickname";
            } else if (lower.contains("email") || lower.contains("почт")) {
                field = "email";
            }
        }
        Map<String, String> details = field != null
                ? Map.of(field, exception.getMessage())
                : Map.of();
        return problem(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", exception.getMessage(), details);
    }

    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<ProblemDetail> invalidVerificationCode(InvalidVerificationCodeException exception) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_VERIFICATION_CODE", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException exception) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            details.putIfAbsent(error.getField(), error.getDefaultMessage() == null ? "Недопустимое значение" : error.getDefaultMessage());
        }
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Проверьте переданные данные", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> unreadableRequest() {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Не удалось прочитать тело запроса", Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> unexpected(Exception exception) {
        log.error("Unexpected error while handling an authentication request", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Внутренняя ошибка сервера", Map.of());
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String message, Map<String, String> details) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, message);
        problem.setType(URI.create("about:blank"));
        problem.setTitle(status.getReasonPhrase());
        problem.setProperty("code", code);
        problem.setProperty("message", message);
        problem.setProperty("details", details);
        return ResponseEntity.status(status).body(problem);
    }
}
