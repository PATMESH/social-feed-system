package com.dev.userauthservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaDebug {
    public KafkaDebug(@Value("${spring.kafka.bootstrap-servers}") String servers) {
        log.info("Kafka bootstrap servers at runtime = {}", servers);
    }
}

