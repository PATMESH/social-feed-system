package com.dev.user_post_service.repository;

import com.dev.user_post_service.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends ReactiveCrudRepository<Post, UUID> {

    Flux<Post> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Flux<Post> findByUserIdInAndIsDeletedFalseOrderByCreatedAtDesc(List<UUID> userIds, Pageable pageable);
}

