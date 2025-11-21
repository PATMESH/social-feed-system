package com.dev.user_post_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class PostResponse {
    private UUID id;
    private UUID userId;
    private String content;
    private String mediaUrl;
    private Instant createdAt;
}

