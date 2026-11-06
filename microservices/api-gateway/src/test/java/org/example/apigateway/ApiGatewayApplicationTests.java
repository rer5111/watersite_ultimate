package org.example.apigateway;

import org.example.apigateway.filter.StripInternalAuthHeaderFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest(properties = {
        "GATEWAY_JWT_ENABLED=true",
        "JWT_JWK_SET_URI=http://localhost:1/.well-known/jwks.json",
        "spring.cloud.gateway.routes[0].id=fallback",
        "spring.cloud.gateway.routes[0].uri=http://127.0.0.1:1",
        "spring.cloud.gateway.routes[0].predicates[0]=Path=/**",
        "springdoc.swagger-ui.urls[0].name=test-service",
        "springdoc.swagger-ui.urls[0].url=/api/test-service/v3/api-docs"
})
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class ApiGatewayApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private StripInternalAuthHeaderFilter stripInternalAuthHeaderFilter;

    @Test
    void contextLoads() {
    }

    @Test
    void internalEndpointsAreDenied() {
        webTestClient.get()
                .uri("/api/player-service/internal/auth/verify")
                .exchange()
                .expectStatus().is4xxClientError();

        webTestClient.post()
                .uri("/internal/notifications/email/send-code")
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void downstreamActuatorMetricsRequireAdministratorRole() {
        webTestClient.get()
                .uri("/api/skin-service/actuator/metrics")
                .exchange()
                .expectStatus().is4xxClientError();

        webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .get()
                .uri("/api/skin-service/actuator/metrics")
                .exchange()
                .expectStatus().isForbidden();

        webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .get()
                .uri("/api/skin-service/actuator/metrics")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void swaggerUiIsServedLocally() {
        String uiLocation = webTestClient.get()
                .uri("/swagger-ui.html")
                .exchange()
                .expectStatus().is3xxRedirection()
                .returnResult(Void.class)
                .getResponseHeaders()
                .getLocation()
                .toString();

        webTestClient.get()
                .uri(uiLocation)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Swagger UI"));

        webTestClient.get()
                .uri("/v3/api-docs/swagger-config")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("/api/test-service/v3/api-docs"));
    }

    @Test
    void filterStripsInternalAuthHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(StripInternalAuthHeaderFilter.INTERNAL_AUTH_HEADER, "secret-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        stripInternalAuthHeaderFilter.filter(exchange, mutatedExchange -> {
            assertThat(mutatedExchange.getRequest().getHeaders().containsKey(StripInternalAuthHeaderFilter.INTERNAL_AUTH_HEADER))
                    .isFalse();
            return Mono.empty();
        }).block();
    }
}
