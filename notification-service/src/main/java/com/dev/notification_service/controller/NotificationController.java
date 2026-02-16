package com.dev.notification_service.controller;

import com.dev.notification_service.dto.response.APIResponse;
import com.dev.notification_service.dto.response.NotificationResponse;
import com.dev.notification_service.dto.response.PaginatedResponse;
import com.dev.notification_service.service.NotificationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationQueryService notificationQueryService;

    @GetMapping
    public Mono<APIResponse<PaginatedResponse<NotificationResponse>>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return notificationQueryService.getMyNotifications(page, size);
    }

}
