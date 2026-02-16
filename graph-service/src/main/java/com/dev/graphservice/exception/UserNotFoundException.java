package com.dev.graphservice.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID userId) {
        super("User not found with userId: " + userId);
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
