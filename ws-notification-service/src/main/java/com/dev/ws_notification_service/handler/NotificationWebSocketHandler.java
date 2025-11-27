package com.dev.ws_notification_service.handler;

import com.dev.ws_notification_service.service.PresenceService;
import com.dev.ws_notification_service.service.RedisSubscriber;
import com.dev.ws_notification_service.service.UserSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketHandler implements WebSocketHandler {

    private final PresenceService presence;
    private final UserSessionRegistry registry;
    private final RedisSubscriber subscriber;

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        String header = session.getHandshakeInfo().getHeaders().getFirst("X-User-Id");
        if (header == null) return session.close();

        UUID userId = UUID.fromString(header);

        registry.register(userId, session);

        return presence.setOnline(userId, subscriber.getNodeId())
                .then(session.receive()
                        .doFinally(s -> {
                            registry.remove(userId);
                            presence.removeOnline(userId).subscribe();
                        })
                        .then());
    }
}
