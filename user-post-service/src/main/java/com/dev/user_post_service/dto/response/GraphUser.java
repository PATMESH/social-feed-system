package com.dev.user_post_service.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class GraphUser {
    private Object id;
    private UUID userId;
}

