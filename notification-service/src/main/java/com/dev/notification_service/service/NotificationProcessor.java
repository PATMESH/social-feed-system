package com.dev.notification_service.service;

import com.dev.notification_service.entity.Notification;
import com.dev.notification_service.kafka.event.UserNotificationEvent;
import com.dev.notification_service.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProcessor {

    private final NotificationRepository notificationRepository;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper mapper;

    private final String PRESENCE_KEY = "presence:";

    public void processEvent(UserNotificationEvent evt, String eventType, Acknowledgment ack) {
        List<Notification> rows = evt.getNotifiers().stream()
                .map(receiver -> Notification.builder()
                        .userId(receiver)
                        .actorId(evt.getActorId())
                        .type(eventType)
                        .message(evt.getMessage())
                        .resourceId(evt.getPostId() != null ? evt.getPostId() :evt.getActorId())
                        .createdAt(Instant.now())
                        .isRead(false)
                        .build())
                .toList();

        notificationRepository.saveAll(rows)
                .collectList()
                .flatMap(this::publishToRedis)
                .doOnSuccess(v -> {
                    ack.acknowledge();
                    log.info("Notifications persisted & ACK committed successfully.");
                })
                .doOnError(err -> log.error("Notification processing failed", err))
                .block();
    }

    private Mono<Void> publishToRedis(List<Notification> saved) {

        List<Mono<Void>> ops = new ArrayList<>();

        for (Notification n : saved) {
            UUID userId = n.getUserId();
            String presenceKey = PRESENCE_KEY + n.getUserId();

            ops.add(
                    redisTemplate.opsForValue()
                            .get(presenceKey)
                            .flatMap(nodeId -> {
                                if (nodeId == null) {
                                    return Mono.empty();
                                }

                                String channel = "node:" + nodeId;

                                String payload = toJson(Map.of(
                                        "userId", userId.toString(),
                                        "notification", n
                                ));

                                return redisTemplate.convertAndSend(channel, payload).then();
                            })
            );
        }

        return Mono.when(ops);
    }

    private String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        }
        catch (Exception e) {
            log.info("Error converting json to string: " + e.getMessage());
            return "{}";
        }
    }
}
