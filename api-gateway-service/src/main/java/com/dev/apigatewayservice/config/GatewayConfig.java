package com.dev.apigatewayservice.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

//    @Bean
//    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("user-service", r -> r.path("/user/**").filters(f -> f.stripPrefix(1)).uri("http://localhost:8081"))
//                .route("graph-service", r -> r.path("/graph/**").filters(f -> f.stripPrefix(1)).uri("http://localhost:8082"))
//                .route("post-service", r -> r.path("/post/**").filters(f -> f.stripPrefix(1)).uri("http://localhost:8083"))
//                .route("notification-service", r -> r.path("/notification/**").filters(f -> f.stripPrefix(1)).uri("http://localhost:8084"))
//                .route("ws-notification", r -> r.path("/ws/notification/**").filters(f -> f.stripPrefix(2)).uri("ws://localhost:8084"))
//                .build();
//    }
}