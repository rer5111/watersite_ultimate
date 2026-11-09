package org.example.apigateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitBreakerConfiguration {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer(
            CircuitBreakerRegistry circuitBreakerRegistry,
            TimeLimiterRegistry timeLimiterRegistry
    ) {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(circuitBreakerRegistry.find(id)
                        .map(CircuitBreaker::getCircuitBreakerConfig)
                        .orElseGet(circuitBreakerRegistry::getDefaultConfig))
                .timeLimiterConfig(timeLimiterRegistry.find(id)
                        .map(TimeLimiter::getTimeLimiterConfig)
                        .orElseGet(() -> TimeLimiterConfig.custom()
                                .timeoutDuration(DEFAULT_TIMEOUT)
                                .cancelRunningFuture(true)
                                .build()))
                .build());
    }
}
