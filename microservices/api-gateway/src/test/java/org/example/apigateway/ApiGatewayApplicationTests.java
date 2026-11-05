package org.example.apigateway;

import org.example.apigateway.filter.StripInternalAuthHeaderFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
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
