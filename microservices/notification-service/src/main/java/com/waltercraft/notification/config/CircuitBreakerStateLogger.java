package com.waltercraft.notification.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CircuitBreakerStateLogger {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerStateLogger.class);

    public CircuitBreakerStateLogger(CircuitBreakerRegistry registry) {
        registry.getAllCircuitBreakers().forEach(this::register);
        registry.getEventPublisher().onEntryAdded(event -> register(event.getAddedEntry()));
    }

    private void register(CircuitBreaker circuitBreaker) {
        circuitBreaker.getEventPublisher().onStateTransition(event -> log.warn(
                "Circuit breaker state changed circuitBreaker={} from={} to={}",
                circuitBreaker.getName(),
                event.getStateTransition().getFromState(),
                event.getStateTransition().getToState()));
    }
}
