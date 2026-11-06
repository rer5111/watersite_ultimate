package org.example.userservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiProblemResponse> unexpected(Exception exception, WebRequest request) {
        String method = request instanceof ServletWebRequest servletRequest
                ? servletRequest.getRequest().getMethod() : "UNKNOWN";
        String path = request instanceof ServletWebRequest servletRequest
                ? servletRequest.getRequest().getRequestURI() : "UNKNOWN";
        log.error("Unhandled request failure method={} path={} exceptionType={}",
                method, path, exception.getClass().getSimpleName(), exception);
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
