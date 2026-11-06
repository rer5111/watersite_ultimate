package com.waltercraft.skin.service;

import com.sun.net.httpserver.HttpServer;
import com.waltercraft.skin.config.SkinProperties;
import com.waltercraft.skin.exception.SkinProviderUnavailableException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class SkinServiceTest {

    private static final byte[] IMAGE = "test-png".getBytes(StandardCharsets.UTF_8);

    private HttpServer server;
    private final AtomicInteger requests = new AtomicInteger();
    private volatile int responseStatus;

    @BeforeEach
    void startProvider() throws IOException {
        requests.set(0);
        responseStatus = 200;
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            if (responseStatus == 200) {
                exchange.getResponseHeaders().add("Content-Type", "image/png");
                exchange.sendResponseHeaders(200, IMAGE.length);
                try (OutputStream output = exchange.getResponseBody()) {
                    output.write(IMAGE);
                }
            } else {
                exchange.sendResponseHeaders(responseStatus, -1);
                exchange.close();
            }
        });
        server.start();
    }

    @AfterEach
    void stopProvider() {
        server.stop(0);
    }

    @Test
    void servesProviderImageAndCachesIt() {
        SkinService service = new SkinService(properties(), CircuitBreakerRegistry.ofDefaults());

        Optional<byte[]> first = service.getFullBySubject("X-Steve");
        Optional<byte[]> cached = service.getFullBySubject("X-Steve");

        assertThat(first).isPresent();
        assertThat(cached).isPresent();
        assertArrayEquals(IMAGE, first.orElseThrow());
        assertArrayEquals(IMAGE, cached.orElseThrow());
        assertThat(requests).hasValue(1);
    }

    @Test
    void opensCircuitOnProviderFailureAndStopsSendingRequests() {
        responseStatus = 503;
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(1)
                .minimumNumberOfCalls(1)
                .failureRateThreshold(100)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .recordExceptions(SkinProviderUnavailableException.class)
                .build();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        SkinService service = new SkinService(properties(), registry);

        assertThatThrownBy(() -> service.getFullBySubject("X-Steve"))
                .isInstanceOf(SkinProviderUnavailableException.class);
        assertThat(registry.circuitBreaker("skin-127.0.0.1").getState()).isEqualTo(CircuitBreaker.State.OPEN);
        assertThat(requests).hasValue(1);
    }

    private SkinProperties properties() {
        String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        return new SkinProperties(baseUrl, baseUrl, baseUrl,
                "WaltercraftSkinService/1.0", 1440, 1000, 1000);
    }
}
