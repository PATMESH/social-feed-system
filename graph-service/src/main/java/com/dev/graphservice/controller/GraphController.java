package com.dev.graphservice.controller;

import com.dev.graphservice.config.RequestContext;
import com.dev.graphservice.dto.ApiResponse;
import com.dev.graphservice.model.User;
import com.dev.graphservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class GraphController {

    private final UserService userService;
    private final RequestContext requestContext;

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
        try {
            User saved = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User created successfully", saved));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create user", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable UUID id) {
        try {
            return userService.findByUserId(id)
                    .map(user -> ResponseEntity.ok(ApiResponse.success("User found", user)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("User not found with id: " + id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving user", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getUsers() {
        try {
            List<User> users =  userService.getAllUsers();
            return ResponseEntity.ok(ApiResponse.success("Users fetched successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving users", e.getMessage()));
        }
    }

    @PostMapping("/follow/{toUserId}")
    public ResponseEntity<ApiResponse<Void>> followUser(@PathVariable UUID toUserId) {
        try {
            UUID fromUserId = UUID.fromString(requestContext.getUserId());
            userService.followUser(fromUserId, toUserId, "following", requestContext.getCorrelationId());
            return ResponseEntity.ok(ApiResponse.success("Users linked successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error linking users", e.getMessage()));
        }
    }

    @PostMapping("/follow/{toUserId}/{fromUserId}")
    public ResponseEntity<ApiResponse<Void>> followUser(@PathVariable UUID toUserId, @PathVariable UUID fromUserId) {
        try {
            userService.followUser(fromUserId, toUserId, "following", requestContext.getCorrelationId());
            return ResponseEntity.ok(ApiResponse.success("Users linked successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error linking users", e.getMessage()));
        }
    }

    @PostMapping("/unfollow/{toUserId}")
    public ResponseEntity<ApiResponse<Void>> unFollowUser(@PathVariable UUID toUserId) {
        try {
            UUID fromUserId = UUID.fromString(requestContext.getUserId());
            userService.unFollowUser(fromUserId, toUserId, "following");
            return ResponseEntity.ok(ApiResponse.success("Relation deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting relation", e.getMessage()));
        }
    }

    @GetMapping("/followings")
    public ResponseEntity<ApiResponse<List<User>>> getFollowings(
            @RequestParam(required = false) UUID userId
    ) {
        try {
            UUID resolvedUserId = (userId != null) ? userId : UUID.fromString(requestContext.getUserId());
            List<User> followings = userService.getFollowings(resolvedUserId);
            return ResponseEntity.ok(ApiResponse.success("Followings fetched successfully", followings));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving followings", e.getMessage()));
        }
    }

    @GetMapping("/followers")
    public ResponseEntity<ApiResponse<List<User>>> getFollowers(
            @RequestParam(required = false) UUID userId
    ) {
        try {
            UUID resolvedUserId = (userId != null) ? userId : UUID.fromString(requestContext.getUserId());
            List<User> followers = userService.getFollowers(resolvedUserId);
            return ResponseEntity.ok(ApiResponse.success("Followers fetched successfully", followers));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving followers", e.getMessage()));
        }
    }

    @GetMapping("/connections")
    public ResponseEntity<ApiResponse<List<User>>> getConnections(
            @RequestParam(required = false) UUID userId
    ) {
        try {
            UUID resolvedUserId = (userId != null) ? userId : UUID.fromString(requestContext.getUserId());
            List<User> connections = userService.getBidirectionalConnections(resolvedUserId);
            return ResponseEntity.ok(ApiResponse.success("Connections fetched successfully", connections));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving connections", e.getMessage()));
        }
    }
}