package com.dev.ws_notification_service.service;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserSessionRegistry {

    private final Map<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void register(UUID userId, WebSocketSession session) {
        sessions.put(userId, session);
    }

    public void remove(UUID userId) {
        sessions.remove(userId);
    }

    public Optional<WebSocketSession> get(UUID userId) {
        return Optional.ofNullable(sessions.get(userId));
    }
}

