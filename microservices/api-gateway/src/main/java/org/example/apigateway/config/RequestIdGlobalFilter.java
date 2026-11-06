package org.example.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class RequestIdGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestIdGlobalFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final Pattern SAFE_REQUEST_ID = Pattern.compile("[A-Za-z0-9._:-]{1,64}");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String suppliedRequestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
        String requestId = suppliedRequestId != null && SAFE_REQUEST_ID.matcher(suppliedRequestId).matches()
                ? suppliedRequestId
                : UUID.randomUUID().toString();

        ServerWebExchange routedExchange = exchange.mutate()
                .request(exchange.getRequest().mutate().headers(headers -> headers.set(REQUEST_ID_HEADER, requestId)).build())
                .build();
        routedExchange.getResponse().beforeCommit(() -> {
            routedExchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, requestId);
            return Mono.empty();
        });
        long startedAt = System.nanoTime();

        return chain.filter(routedExchange)
                .doFinally(signal -> {
                    HttpStatusCode status = routedExchange.getResponse().getStatusCode();
                    long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
                    log.info("Gateway request completed requestId={} status={} durationMs={} signal={}",
                            requestId, status == null ? 0 : status.value(), durationMs, signal);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
