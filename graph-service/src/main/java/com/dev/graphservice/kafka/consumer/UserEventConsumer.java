package com.dev.graphservice.kafka.consumer;

import com.dev.graphservice.kafka.event.CloudEvent;
import com.dev.graphservice.kafka.event.UserCreatedEvent;
import com.dev.graphservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventConsumer {

    private final UserService graphUserService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.user-events}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeUserCreatedEvent(
            CloudEvent<?> event,
            @Header("event-type") String eventType,
            @Header("correlation-id") String correlationId
    ) {
        UserCreatedEvent userEvent = convert(event);

        log.debug("Processing UserCreatedEvent: {}", userEvent.getUserId());
        graphUserService.createUserFromEvent(userEvent);
    }

    private UserCreatedEvent convert(CloudEvent<?> event) {
        return objectMapper.convertValue(event.getData(), UserCreatedEvent.class);
    }
}

