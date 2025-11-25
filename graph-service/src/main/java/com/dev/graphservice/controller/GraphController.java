package com.dev.graphservice.controller;

import com.dev.graphservice.dto.ApiResponse;
import com.dev.graphservice.model.User;
import com.dev.graphservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class GraphController {

    private final UserService userService;

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

    @PostMapping("/map")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createUserMap(@RequestBody Map<String, Object> userMap) {
        try {
            Map<String, Object> saved = userService.saveUser(userMap);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User map saved successfully", saved));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to save user map", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        try {
            return userService.findUserById(id)
                    .map(user -> ResponseEntity.ok(ApiResponse.success("User found", user)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("User not found with id: " + id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving user", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(ApiResponse.success("All users fetched successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving users", e.getMessage()));
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<User>> findByEmail(@PathVariable String email) {
        try {
            return userService.findByEmail(email)
                    .map(user -> ResponseEntity.ok(ApiResponse.success("User found", user)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("User not found with email: " + email)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving user", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting user", e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAllUsers() {
        try {
            userService.deleteAllUsers();
            return ResponseEntity.ok(ApiResponse.success("All users deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting users", e.getMessage()));
        }
    }

    @PostMapping("/{fromUserId}/follow/{toUserId}")
    public ResponseEntity<ApiResponse<Void>> followUsers(
            @PathVariable Long fromUserId,
            @PathVariable Long toUserId,
            @RequestParam(defaultValue = "following") String relation) {
        try {
            userService.followUsers(fromUserId, toUserId, relation);
            return ResponseEntity.ok(ApiResponse.success("Users linked successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error linking users", e.getMessage()));
        }
    }

    @DeleteMapping("/{fromUserId}/unfollow/{toUserId}")
    public ResponseEntity<ApiResponse<Void>> unFollowUsers(
            @PathVariable Long fromUserId,
            @PathVariable Long toUserId,
            @RequestParam(defaultValue = "following") String relation) {
        try {
            userService.deleteUserRelation(fromUserId, toUserId, relation);
            return ResponseEntity.ok(ApiResponse.success("Relation deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting relation", e.getMessage()));
        }
    }

    @GetMapping("/{userId}/followings")
    public ResponseEntity<ApiResponse<List<User>>> getFollowings(@PathVariable Long userId) {
        try {
            List<User> followings = userService.getFollowings(userId);
            return ResponseEntity.ok(ApiResponse.success("Followings fetched successfully", followings));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving followings", e.getMessage()));
        }
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<ApiResponse<List<User>>> getFollowers(@PathVariable Long userId) {
        try {
            List<User> followers = userService.getFollowers(userId);
            return ResponseEntity.ok(ApiResponse.success("Followers fetched successfully", followers));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving followers", e.getMessage()));
        }
    }

    @GetMapping("/{userId}/connections")
    public ResponseEntity<ApiResponse<List<User>>> getBidirectionalConnections(@PathVariable Long userId) {
        try {
            List<User> connections = userService.getBidirectionalConnections(userId);
            return ResponseEntity.ok(ApiResponse.success("Connections fetched successfully", connections));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving connections", e.getMessage()));
        }
    }
}