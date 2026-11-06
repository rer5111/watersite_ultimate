package org.example.apigateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import static org.assertj.core.api.Assertions.assertThat;

class CircuitBreakerFallbackControllerTest {

    private final CircuitBreakerFallbackController controller = new CircuitBreakerFallbackController();

    @Test
    void returnsControlledServiceUnavailableProblemWithRequestId() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/player-service/api/user/me")
                .header("X-Request-Id", "test-request-123"));

        var problem = controller.serviceUnavailable("player-service", exchange);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThat(problem.getProperties()).containsEntry("code", "SERVICE_UNAVAILABLE");
        assertThat(problem.getProperties()).containsEntry("details", java.util.Map.of("service", "player-service"));
    }

    @Test
    void doesNotEchoUnknownServiceName() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/fallback/unknown"));

        var problem = controller.serviceUnavailable("unknown", exchange);

        assertThat(problem.getProperties()).containsEntry("details", java.util.Map.of("service", "backend-service"));
    }
}
