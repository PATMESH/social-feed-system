package com.dev.user_post_service.client;

import com.dev.user_post_service.dto.response.FollowingsResponse;
import com.dev.user_post_service.dto.response.GraphUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GraphClient {

    @Value("${app.graph-service-url}")
    private String graphService;

    private final WebClient webClient = WebClient.builder().build();

    public Mono<List<UUID>> getFollowings(UUID userId) {
        return webClient.get()
                .uri(graphService + "/api/users/followings/userId/{userId}", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<FollowingsResponse<List<GraphUser>>>() {})
                .doOnNext(resp -> {
                    log.info("[GraphService] Raw followings response for {} -> {}",
                            userId, resp);
                })
                .map(resp -> resp.getData()
                        .stream()
                        .map(GraphUser::getUserId)
                        .toList()
                )
                .switchIfEmpty(Mono.just(List.of()))
                .onErrorResume(e -> {
                    log.error("Failed to fetch followings for {}: {}", userId, e.getMessage());
                    return Mono.just(List.of());
                });
    }
}
