package com.dev.graphservice.service;

import com.dev.graphservice.kafka.event.PostCreatedEvent;
import com.dev.graphservice.kafka.event.UserNotificationEvent;
import com.dev.graphservice.kafka.producer.KafkaEventProducer;
import com.dev.graphservice.model.User;
import com.dev.graphservice.utils.BatchUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostFanoutService {

    private final UserService userService;
    private final KafkaEventProducer kafkaEventProducer;

    @Value("${app.notification.batch-size:2000}")
    private int batchSize;

    @Value("${app.kafka.topics.notification-events}")
    private String notificationTopic;

    public void process(PostCreatedEvent postEvent, String correlationId) {

        log.info("Fan-out processing for Post {}", postEvent.getPostId());

        Optional<User> postCreatedUser = userService.findByUserId(postEvent.getUserId());

        List<User> followers = userService.getFollowingsByUserId(postEvent.getUserId());

        if (followers.isEmpty()) {
            log.info("No followers found for user {}", postEvent.getUserId());
            return;
        }

        List<UUID> followerIds = followers.stream()
                .map(User::getUserId)
                .toList();

        List<List<UUID>> batches = BatchUtils.partition(followerIds, batchSize);

        String message = "New post from " +
                postCreatedUser.map(User::getName).orElse("someone you follow");

        batches.forEach(batch -> {
            UserNotificationEvent eventPayload = UserNotificationEvent.builder()
                    .message(message)
                    .actorId(postEvent.getUserId())
                    .notifiers(batch)
                    .postId(postEvent.getPostId())
                    .metadata(Map.of())
                    .build();

            kafkaEventProducer.publishEvent(
                    notificationTopic,
                    "POST-NOTIFICATION",
                    postEvent.getUserId().toString(),
                    eventPayload,
                    correlationId
            );
        });

        log.info("Fan-out completed. Total {} followers in {} batches",
                followerIds.size(),
                batches.size());
    }
}

