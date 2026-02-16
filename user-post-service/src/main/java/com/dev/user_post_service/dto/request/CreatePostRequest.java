package com.dev.user_post_service.dto.request;

import lombok.Data;

@Data
public class CreatePostRequest {
    private String content;
    private String mediaUrl;
}

