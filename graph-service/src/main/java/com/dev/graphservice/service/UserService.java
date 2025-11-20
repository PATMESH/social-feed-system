package com.dev.graphservice.service;

import com.dev.graphservice.core.GremlinGraphRepository;
import com.dev.graphservice.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public void linkUsers(Object fromUserId, Object toUserId, String relationLabel) {
        repo.createEdgeBetween(fromUserId, toUserId, relationLabel, null);
    }

    public void deleteUserRelation(Object fromUserId, Object toUserId, String relationLabel) {
        repo.deleteEdge(fromUserId, toUserId, relationLabel);
        log.info("Deleted relation {} between users {} and {}", relationLabel, fromUserId, toUserId);
    }

    public List<User> getUserFriends(Object userId) {
        return repo.traverseOutgoing(User.class, userId, "FRIEND_OF");
    }

    public List<User> getFriendsOf(Object userId) {
        return repo.traverseIncoming(User.class, userId, "FRIEND_OF");
    }

    public List<User> getBidirectionalConnections(Object userId) {
        return repo.traverseBoth(User.class, userId, "FRIEND_OF");
    }
}
