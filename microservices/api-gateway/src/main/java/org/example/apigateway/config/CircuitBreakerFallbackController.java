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
    private static final Set<String> SERVICES = Set.of(
            "auth-service", "player-service", "application-service", "payment-service", "donation-service",
            "chat-service", "notification-service", "event-service", "skin-service");

    @RequestMapping("/fallback/{service}")
    public ProblemDetail serviceUnavailable(@PathVariable String service, ServerWebExchange exchange) {
        String serviceName = SERVICES.contains(service) ? service : "backend-service";
        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
        log.warn("Gateway circuit fallback invoked service={} requestId={}", serviceName, requestId);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Сервис временно недоступен. Повторите попытку позже.");
        problem.setTitle("Сервис временно недоступен");
        problem.setType(URI.create("urn:problem:service-unavailable"));
        problem.setProperty("code", "SERVICE_UNAVAILABLE");
        problem.setProperty("message", problem.getDetail());
        problem.setProperty("details", Map.of("service", serviceName));
        return problem;
    }
}
