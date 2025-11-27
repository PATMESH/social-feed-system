package com.dev.notification_service.service;

import com.dev.notification_service.dto.response.APIResponse;
import com.dev.notification_service.dto.response.NotificationResponse;
import com.dev.notification_service.dto.response.PaginatedResponse;
import com.dev.notification_service.entity.Notification;
import com.dev.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationQueryService {
    private final NotificationRepository repository;

    public Mono<APIResponse<PaginatedResponse<NotificationResponse>>> getMyNotifications(int page, int size) {

        return Mono.deferContextual(ctx -> {
            UUID userId = UUID.fromString(ctx.get("userId"));

            PageRequest pageable = PageRequest.of(page, size);

            Mono<List<NotificationResponse>> listMono =
                    repository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                            .map(this::toResponse)
                            .collectList();

            Mono<Long> countMono =
                    repository.countByUserId(userId);

            return Mono.zip(listMono, countMono)
                    .map(tuple -> {

                        List<NotificationResponse> list = tuple.getT1();
                        long total = tuple.getT2();

                        boolean hasNext = ((long) (page + 1) * size) < total;

                        return APIResponse.success(
                                PaginatedResponse.<NotificationResponse>builder()
                                        .items(list)
                                        .page(page)
                                        .size(size)
                                        .hasNext(hasNext)
                                        .build()
                        );
                    });
        });
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .actorId(n.getActorId())
                .type(n.getType())
                .message(n.getMessage())
                .resourceId(n.getResourceId())
                .createdAt(n.getCreatedAt())
                .isRead(n.getIsRead())
                .build();
    }
}
