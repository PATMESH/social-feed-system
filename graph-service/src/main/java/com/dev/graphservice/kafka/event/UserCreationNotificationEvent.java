package com.dev.graphservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationNotificationEvent {
    private Long userId;
    private String username;
    private String email;
    private Instant createdAt;
}
