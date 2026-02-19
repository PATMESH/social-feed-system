package com.dev.graphservice.service;

import com.dev.graphservice.core.GraphRepository;
import com.dev.graphservice.exception.UserNotFoundException;
import com.dev.graphservice.kafka.event.UserCreatedEvent;
import com.dev.graphservice.kafka.event.UserNotificationEvent;
import com.dev.graphservice.kafka.producer.KafkaEventProducer;
import com.dev.graphservice.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.dev.graphservice.enums.Direction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final GraphRepository repo;
    private final KafkaEventProducer eventProducer;

    @Value("${app.kafka.topics.notification-events}")
    private String notificationTopic;

    public User createUser(User user) {
        return repo.save(user);
    }

    public Optional<User> findUserById(Object id) {
        return repo.findById(User.class, id);
    }

    public List<User> getAllUsers() {
        return repo.findAll(User.class);
    }

    public Optional<User> findByEmail(String email) {
        return repo.findByProperty(User.class, "email", email);
    }

    public Optional<User> findByUserId(UUID userId) {
        return repo.findByProperty(User.class, "userId", userId);
    }

    public void deleteUser(UUID id) {
        repo.deleteByProperty(User.class, "userId", id);
    }

    private Object getVertexIdByUserId(UUID userId) {
        return findByUserId(userId)
                .map(User::getId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public void followUser(UUID fromUserUuid, UUID toUserUuid, String relationLabel, String correlationId) {
        Optional<User> fromUser = findByUserId(fromUserUuid);
        fromUser.orElseThrow(() -> new UserNotFoundException(fromUserUuid));
        Object fromVertexId = fromUser.map(User::getId).orElseThrow(() -> new RuntimeException("User not found"));;
        Object toVertexId = getVertexIdByUserId(toUserUuid);
        repo.createEdgeBetween(fromVertexId, toVertexId, relationLabel, Direction.OUT);
        log.info("Created {} relation from {} -> {}", relationLabel, fromUserUuid, toUserUuid);

        UserNotificationEvent eventPayload = UserNotificationEvent.builder()
                .message(fromUser.get().getName())
                .actorId(fromUserUuid)
                .notifiers(new ArrayList<>(List.of(toUserUuid)))
                .build();

        eventProducer.publishEvent(
                notificationTopic,
                "FOLLOW-NOTIFICATION",
                toUserUuid.toString(),
                eventPayload,
                correlationId
        );
    }

    public void unFollowUser(UUID fromUserUuid, UUID toUserUuid, String relationLabel) {
        Object fromVertexId = getVertexIdByUserId(fromUserUuid);
        Object toVertexId = getVertexIdByUserId(toUserUuid);
        repo.deleteEdge(fromVertexId, toVertexId, relationLabel);
        log.info("Deleted {} relation from {} -> {}", relationLabel, fromUserUuid, toUserUuid);
    }

    public List<User> getFollowingsByUserId(UUID userId) {
        try {
            return getFollowings(userId);
        } catch (RuntimeException ex) {
            log.error("Failed to fetch followings for userId {}: {}", userId, ex.getMessage());
            return List.of();
        }
    }

    public List<User> getFollowings(UUID userId) {
        Object vertexId = getVertexIdByUserId(userId);
        return repo.traverseOutgoing(User.class, vertexId, "following");
    }

    public List<User> getFollowers(UUID userId) {
        Object vertexId = getVertexIdByUserId(userId);
        return repo.traverseIncoming(User.class, vertexId, "following");
    }

    public List<User> getBidirectionalConnections(UUID userId) {
        Object vertexId = getVertexIdByUserId(userId);
        return repo.traverseBoth(User.class, vertexId, "following");
    }

    public void createUserFromEvent(UserCreatedEvent userEvent) {
        User user = User.builder()
                .userId(userEvent.getUserId())
                .name(userEvent.getUsername())
                .email(userEvent.getEmail())
                .build();
        createUser(user);
        log.info("Created User: {}", user);
    }
}