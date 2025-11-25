package com.dev.user_post_service.dto.response;

import lombok.Data;

@Data
public class FollowingsResponse<T> {
    private String status;
    private String message;
    private T data;
}

