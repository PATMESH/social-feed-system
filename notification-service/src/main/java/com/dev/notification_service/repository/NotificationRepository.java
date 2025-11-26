package com.dev.notification_service.repository;

import com.dev.notification_service.entity.Notification;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface NotificationRepository extends ReactiveCrudRepository<Notification, Long> {

    Flux<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

}


