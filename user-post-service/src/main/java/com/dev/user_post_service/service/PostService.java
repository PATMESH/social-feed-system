package com.dev.user_post_service.service;

import com.dev.user_post_service.client.GraphClient;
import com.dev.user_post_service.dto.request.CreatePostRequest;
import com.dev.user_post_service.dto.response.APIResponse;
import com.dev.user_post_service.dto.response.PaginatedResponse;
import com.dev.user_post_service.dto.response.PostResponse;
import com.dev.user_post_service.entity.Post;
import com.dev.user_post_service.kafka.producer.KafkaEventProducer;
import com.dev.user_post_service.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final GraphClient graphClient;
    private final KafkaEventProducer postEventProducer;

    @Value("${app.kafka.topics.post-events}")
    private String postEventTopic;

    public Mono<APIResponse<PostResponse>> createPost(CreatePostRequest req) {
        return Mono.deferContextual(ctx -> {
            UUID userId = UUID.fromString(ctx.get("userId"));
            String correlationId = ctx.get("correlationId");

            Post post = Post.builder()
                    .userId(userId)
                    .content(req.getContent())
                    .mediaUrl(req.getMediaUrl())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .isDeleted(false)
                    .build();

            log.info("Creating post {}", post);

            return postRepository.save(post)
                    .flatMap(saved ->
                            postEventProducer.publishEvent(
                                    postEventTopic,
                                    "NEW-POST-EVENT",
                                    userId.toString(),
                                    saved,
                                    correlationId
                            ).thenReturn(APIResponse.success(toResponse(saved)))
                    );
        });
    }

    public Mono<APIResponse<PaginatedResponse<PostResponse>>> getUserPosts(UUID profileUserId, int page, int size) {
        return postRepository
                .findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(profileUserId, PageRequest.of(page, size))
                .map(this::toResponse)
                .collectList()
                .map(list -> APIResponse.success(
                        PaginatedResponse.<PostResponse>builder()
                                .items(list)
                                .page(page)
                                .size(size)
                                .hasNext(list.size() == size)
                                .build()
                ));
    }

    public Mono<APIResponse<PaginatedResponse<PostResponse>>> getMyPosts(int page, int size) {
        return Mono.deferContextual(ctx -> {
            UUID userId = UUID.fromString(ctx.get("userId"));

            return postRepository
                    .findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(
                            userId,
                            PageRequest.of(page, size)
                    )
                    .map(this::toResponse)
                    .collectList()
                    .map(list -> APIResponse.success(
                            PaginatedResponse.<PostResponse>builder()
                                    .items(list)
                                    .page(page)
                                    .size(size)
                                    .hasNext(list.size() == size)
                                    .build()
                    ));
        });
    }

    public Mono<APIResponse<PaginatedResponse<PostResponse>>> getFeed(int page, int size) {
        return Mono.deferContextual(ctx -> {
            UUID userId = UUID.fromString(ctx.get("userId"));

            return graphClient.getFollowings(userId)
                    .flatMapMany(followingIds ->
                            postRepository.findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc(
                                    followingIds, PageRequest.of(page, size)
                            )
                    )
                    .map(this::toResponse)
                    .collectList()
                    .map(list -> APIResponse.success(
                            PaginatedResponse.<PostResponse>builder()
                                    .items(list)
                                    .page(page)
                                    .size(size)
                                    .hasNext(list.size() == size)
                                    .build()
                    ));
        });
    }

    public Mono<APIResponse<Void>> deletePost(UUID postId) {
        return Mono.deferContextual(ctx -> {
            UUID userId = UUID.fromString(ctx.get("userId"));

            return postRepository.findById(postId)
                    .switchIfEmpty(Mono.error(new RuntimeException("Post not found")))
                    .flatMap(post -> {
                        if (!post.getUserId().equals(userId)) {
                            return Mono.error(new RuntimeException("Unauthorized"));
                        }
                        post.setIsDeleted(true);
                        return postRepository.save(post);
                    })
                    .thenReturn(APIResponse.success(null, "Post deleted"));
        });
    }

    private PostResponse toResponse(Post p) {
        return PostResponse.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .content(p.getContent())
                .mediaUrl(p.getMediaUrl())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
