package com.dev.graphservice.kafka.consumer;

import com.dev.graphservice.kafka.event.CloudEvent;
import com.dev.graphservice.kafka.event.PostCreatedEvent;
import com.dev.graphservice.service.PostFanoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostEventConsumer {

    private final PostFanoutService postFanoutService;
    private final Executor fanoutExecutor;

    @KafkaListener(
            topics = "${app.kafka.topics.post-events}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeUserCreatedEvent(
            CloudEvent<PostCreatedEvent> event,
            @Header("event-type") String eventType,
            @Header("correlation-id") String correlationId
    ) {
        fanoutExecutor.execute(() -> {
            try {
                PostCreatedEvent postCreatedEvent = convert(event);
                log.debug("Processing PostCreatedEvent: {}", postCreatedEvent.getUserId());
                postFanoutService.process(postCreatedEvent, correlationId);
            } catch (Exception e) {
                log.error("Error processing PostCreatedEvent", e);
            }
        });
    }

    private PostCreatedEvent convert(CloudEvent<PostCreatedEvent> event) {
        return event.getData();
    }
}

