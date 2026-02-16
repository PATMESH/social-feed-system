package com.dev.notification_service.config;

import lombok.Data;

@Data
public class RequestContext {
    private String correlationId;
    private String userId;
    private String username;
}