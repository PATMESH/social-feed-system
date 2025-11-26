package com.dev.notification_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table("notifications")
public class Notification {
    @Id
    @Column("id")
    private UUID id;

    @JsonIgnore
    @Column("user_id")
    private UUID userId;

    @Column("actor_id")
    private UUID actorId;

    @Column("type")
    private String type;

    @Column("message")
    private String message;

    @Column("resource_id")
    private UUID resourceId;

    @Column("created_at")
    private Instant createdAt;

    @JsonIgnore
    @Column("is_read")
    private Boolean isRead;
}

