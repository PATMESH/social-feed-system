package com.dev.graphservice.service;

import com.dev.graphservice.core.GremlinGraphRepository;
import com.dev.graphservice.kafka.event.UserCreatedEvent;
import com.dev.graphservice.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final GremlinGraphRepository repo;

    public User createUser(User user) {
        return repo.save(user);
    }

    public Map<String, Object> saveUser(Map<String, Object> user) {
        return repo.save("User", user);
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

    public void deleteUser(Object id) {
        repo.delete(id);
    }

    public void deleteAllUsers() {
        repo.deleteAll(User.class);
    }

    public void followUsers(Object fromUserId, Object toUserId, String relationLabel) {
        repo.createEdgeBetween(fromUserId, toUserId, relationLabel, Direction.OUT);
    }

    public void deleteUserRelation(Object fromUserId, Object toUserId, String relationLabel) {
        repo.deleteEdge(fromUserId, toUserId, relationLabel);
        log.info("Deleted relation {} between users {} and {}", relationLabel, fromUserId, toUserId);
    }

    public List<User> getFollowingsByUserId(UUID userId) {
        Optional<User> userOpt = repo.findByProperty(User.class, "userId", userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with userId: " + userId);
        }
        Object vertexId = userOpt.get().getId();
        return getFollowings(vertexId);
    }

    public List<User> getFollowings(Object userId) {
        return repo.traverseOutgoing(User.class, userId, "following");
    }

    public List<User> getFollowers(Object userId) {
        return repo.traverseIncoming(User.class, userId, "following");
    }

    public List<User> getBidirectionalConnections(Object userId) {
        return repo.traverseBoth(User.class, userId, "following");
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