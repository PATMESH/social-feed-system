package com.dev.user_post_service.config;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Data
public class RequestContext {
    private String correlationId;
    private String userId;
    private String username;
}