package com.dev.ws_notification_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSubscriber {

    private final ReactiveRedisTemplate<String, String> redis;
    private final UserSessionRegistry sessionRegistry;
    private final ObjectMapper mapper = new ObjectMapper();

    @Getter
    private final String nodeId = UUID.randomUUID().toString();

    @PostConstruct
    public void subscribe() {
        redis.listenToChannel("node:" + nodeId)
                .map(m -> m.getMessage())
                .flatMap(this::deliver)
                .subscribe();
    }

    private Mono<Void> deliver(String json) {
        try {
            Map<String, Object> map = mapper.readValue(json, Map.class);

            UUID userId = UUID.fromString((String) map.get("userId"));
            Object notification = map.get("notification");

            sessionRegistry.get(userId).ifPresent(session ->
                    session.send(Mono.just(session.textMessage(toJson(notification)))).subscribe()
            );

        } catch (Exception e) {
            log.error("WS delivery failed", e);
        }

        return Mono.empty();
    }

    private String toJson(Object obj) {
        try { return mapper.writeValueAsString(obj); }
        catch (Exception e) {
            log.info("Error converting json to string: " + e.getMessage());
            return "{}";
        }
    }

}

