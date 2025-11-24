package com.dev.user_post_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GraphClient {

    @Value("${app.graph-service-url}")
    private String graphService;

    private final WebClient webClient = WebClient.builder().build();

    public Mono<List<UUID>> getFollowings(UUID userId) {
        return webClient.get()
                .uri(graphService + "/api/v1/graph/followings/{id}", userId)
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        response -> {
                            return Mono.empty();
                        }
                )
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("Client error")
                                .flatMap(body -> Mono.error(
                                        new RuntimeException("Graph service 4xx: " + body)
                                ))
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("Server error")
                                .flatMap(body -> Mono.error(
                                        new RuntimeException("Graph service 5xx: " + body)
                                ))
                )
                .bodyToMono(new ParameterizedTypeReference<List<UUID>>() {})
                .switchIfEmpty(Mono.just(List.of()))
                .onErrorResume(e -> {
                    return Mono.just(List.of());
                });
    }

}
