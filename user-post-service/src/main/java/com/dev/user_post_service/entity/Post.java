package com.dev.user_post_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("posts")
public class Post {
    @Id
    private UUID id;
    private UUID userId;
    private String content;
    private String mediaUrl;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean isDeleted;
}

