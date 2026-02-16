package com.dev.user_post_service.kafka.producer;

import com.dev.user_post_service.kafka.event.CloudEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventProducer {

    private final KafkaTemplate<String, CloudEvent<?>> kafkaTemplate;

    @Value("${spring.application.name:user-auth-service}")
    private String applicationName;

    private static final String SPEC_VERSION = "1.0";
    private static final String CONTENT_TYPE = "application/json";

    public <T> Mono<Void> publishEvent(
            String topic,
            String eventType,
            String key,
            T payload,
            String correlationId) {

        CloudEvent<T> cloudEvent = buildCloudEvent(eventType, payload, correlationId);

        ProducerRecord<String, CloudEvent<?>> record =
                new ProducerRecord<>(topic, key, cloudEvent);

        record.headers()
                .add("correlation-id", correlationId.getBytes(StandardCharsets.UTF_8))
                .add("event-type", eventType.getBytes(StandardCharsets.UTF_8))
                .add("source", applicationName.getBytes(StandardCharsets.UTF_8));

        return Mono.fromFuture(kafkaTemplate.send(record))
                .doOnSuccess(result -> {
                    RecordMetadata metadata = result.getRecordMetadata();
                    log.info("Event published successfully - Topic: {}, Partition: {}, CorrelationId: {}",
                            metadata.topic(), metadata.partition(), correlationId);
                })
                .doOnError(ex -> log.error("Failed to publish event - Topic: {}, Type: {}, Key: {}, CorrelationId: {}",
                        topic, eventType, key, correlationId, ex))
                .then();
    }

    private <T> CloudEvent<T> buildCloudEvent(String eventType, T payload, String correlationId) {
        return CloudEvent.<T>builder()
                .id(UUID.randomUUID().toString())
                .source(applicationName)
                .specVersion(SPEC_VERSION)
                .type(eventType)
                .dataContentType(CONTENT_TYPE)
                .time(Instant.now())
                .correlationId(correlationId)
                .data(payload)
                .build();
    }
}
