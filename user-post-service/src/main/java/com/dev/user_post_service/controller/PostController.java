package com.dev.user_post_service.controller;

import com.dev.user_post_service.dto.request.CreatePostRequest;
import com.dev.user_post_service.dto.response.APIResponse;
import com.dev.user_post_service.dto.response.PaginatedResponse;
import com.dev.user_post_service.dto.response.PostResponse;
import com.dev.user_post_service.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public Mono<APIResponse<PostResponse>> create(@RequestBody CreatePostRequest req) {
        return postService.createPost(req);
    }

    @GetMapping("/user/{userId}")
    public Mono<APIResponse<PaginatedResponse<PostResponse>>> getUserPosts(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return postService.getUserPosts(userId, page, size);
    }

    @GetMapping("/feed")
    public Mono<APIResponse<PaginatedResponse<PostResponse>>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return postService.getFeed(page, size);
    }

    @DeleteMapping("/{postId}")
    public Mono<APIResponse<Void>> delete(@PathVariable UUID postId) {
        return postService.deletePost(postId);
    }
}

