package org.example.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.util.Map;
import java.util.Set;

@RestController
public class CircuitBreakerFallbackController {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerFallbackController.class);
    private static final Map<String, String> SERVICE_DESCRIPTIONS = Map.of(
            "auth-service", "Сервис авторизации",
            "player-service", "Сервис игроков и профилей",
            "application-service", "Сервис заявок",
            "payment-service", "Платежный сервис",
            "donation-service", "Сервис пожертвований",
            "chat-service", "Сервис чата",
            "notification-service", "Сервис уведомлений",
            "season-service", "Сервис сезонов",
            "event-service", "Сервис ивентов",
            "skin-service", "Сервис скинов"
    );

    @RequestMapping("/fallback/{service}")
    public ProblemDetail serviceUnavailable(@PathVariable String service, ServerWebExchange exchange) {
        String serviceName = SERVICE_DESCRIPTIONS.containsKey(service) ? service : "backend-service";
        String serviceTitle = SERVICE_DESCRIPTIONS.getOrDefault(service, "Сервис");
        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
        log.warn("Gateway circuit fallback invoked service={} requestId={}", serviceName, requestId);

        String message = "%s временно недоступен. Повторите попытку позже.".formatted(serviceTitle);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                message);
        problem.setTitle(serviceTitle + " недоступен");
        problem.setType(URI.create("urn:problem:service-unavailable"));
        problem.setProperty("code", "SERVICE_UNAVAILABLE");
        problem.setProperty("message", message);
        problem.setProperty("details", Map.of("service", serviceName));
        return problem;
    }
}
