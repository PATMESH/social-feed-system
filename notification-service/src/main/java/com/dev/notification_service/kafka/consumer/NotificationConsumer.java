package com.dev.notification_service.kafka.consumer;

import com.dev.notification_service.kafka.event.CloudEvent;
import com.dev.notification_service.kafka.event.UserNotificationEvent;
import com.dev.notification_service.service.NotificationProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationProcessor notificationProcessor;

    @KafkaListener(
            topics = "${app.kafka.topics.notification-events}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(CloudEvent<UserNotificationEvent> event,
                        @Header("event-type") String eventType,
                        @Header("correlation-id") String correlationId,
                        Acknowledgment ack
    ) {
        notificationProcessor.processEvent(event.getData(), event.getType(), ack);
    }
}
