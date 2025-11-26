package com.dev.notification_service.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.dev.notification_service.constant.ApplicationConstants.CORRELATION_ID_HEADER;
import static com.dev.notification_service.constant.ApplicationConstants.USER_ID_HEADER;

@Component
@Slf4j
public class CorrelationIdFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        String userId = request.getHeaders().getFirst(USER_ID_HEADER);

        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put("correlationId", correlationId);
        if (userId != null) MDC.put("userId", userId);

        String finalCorrelationId = correlationId;
        return chain.filter(exchange)
                .contextWrite(ctx -> ctx
                        .put("correlationId", finalCorrelationId)
                        .put("userId", userId))
                .doFinally(signal -> MDC.clear());
    }
}
