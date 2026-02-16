package com.dev.user_post_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PaginatedResponse<T> {
    private List<T> items;
    private int page;
    private int size;
    private boolean hasNext;
}

