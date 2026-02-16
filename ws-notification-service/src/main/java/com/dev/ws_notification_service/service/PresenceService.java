package com.dev.ws_notification_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final ReactiveRedisTemplate<String, String> redis;
    private static final Duration TTL = Duration.ofSeconds(60);

    public Mono<Void> setOnline(UUID userId, String nodeId) {
        return redis.opsForValue().set("presence:" + userId, nodeId, TTL).then();
    }

    public Mono<Void> refresh(UUID userId) {
        return redis.opsForValue()
                .getAndExpire("presence:" + userId, TTL)
                .then();
    }

    public Mono<Void> removeOnline(UUID userId) {
        return redis.opsForValue().delete("presence:" + userId).then();
    }
}

