package com.dev.user_post_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
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
    @Column("id")
    private UUID id;

    @Column("user_id")
    private UUID userId;

    @Column("content")
    private String content;

    @Column("media_url")
    private String mediaUrl;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    @Column("is_deleted")
    private Boolean isDeleted;
}

