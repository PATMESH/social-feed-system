package com.dev.graphservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationEvent {
    private String message;
    private UUID actorId;
    private List<UUID> notifiers;
    private UUID postId;
    private Map<String, Object> metadata;
}

