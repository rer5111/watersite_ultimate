package org.example.userservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiProblemResponse> notFound(ResourceNotFoundException exception, WebRequest request) {
        return problem(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiProblemResponse> conflict(ConflictException exception, WebRequest request) {
        return problem(HttpStatus.CONFLICT, "CONFLICT", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiProblemResponse> badRequest(BadRequestException exception, WebRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiProblemResponse> invalidArgument(IllegalArgumentException exception, WebRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiProblemResponse> constraintViolation(
            ConstraintViolationException exception, WebRequest request) {
        Map<String, String> details = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation ->
                details.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Проверьте параметры запроса", request, details);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            details.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ApiProblemResponse body = body(status, "VALIDATION_FAILED", "Проверьте переданные данные", request, details);
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, status);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiProblemResponse> invalidState(IllegalStateException exception, WebRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_STATE", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiProblemResponse> accessDenied(AccessDeniedException exception, WebRequest request) {
        String msg = exception.getMessage() != null && !exception.getMessage().isBlank()
                ? exception.getMessage()
                : "Недостаточно прав для выполнения действия";
        return problem(HttpStatus.FORBIDDEN, "FORBIDDEN", msg, request, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiProblemResponse> dataIntegrityViolation(
            DataIntegrityViolationException exception, WebRequest request) {
        return problem(HttpStatus.CONFLICT, "DATA_CONFLICT",
                "Невозможно выполнить действие: запись связана с другими данными или уже существует", request, Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiProblemResponse> unexpected(Exception exception, WebRequest request) {
        String method = request instanceof ServletWebRequest servletRequest
                ? servletRequest.getRequest().getMethod() : "UNKNOWN";
        log.error("Unhandled request failure method={} exceptionType={}",
                method, exception.getClass().getSimpleName(), exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Внутренняя ошибка сервера", request, Map.of());
    }

    private static ResponseEntity<ApiProblemResponse> problem(
            HttpStatus status,
            String code,
            String message,
            WebRequest request,
            Map<String, String> details) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body(status, code, message, request, details));
    }

    private static ApiProblemResponse body(
            HttpStatus status,
            String code,
            String message,
            WebRequest request,
            Map<String, String> details) {
        String path = request instanceof ServletWebRequest servletRequest
                ? servletRequest.getRequest().getRequestURI() : "/";
        return new ApiProblemResponse(
                URI.create("about:blank").toString(), status.getReasonPhrase(), status.value(),
                message, path, code, message, details);
    }

    public record ApiProblemResponse(
            String type,
            String title,
            int status,
            String detail,
            String instance,
            String code,
            String message,
            Map<String, String> details) {
    }
}
