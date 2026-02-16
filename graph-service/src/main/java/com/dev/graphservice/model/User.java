package com.dev.graphservice.model;

import com.dev.graphservice.annotation.GraphEdge;
import com.dev.graphservice.annotation.GraphVertex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.tinkerpop.gremlin.structure.Direction;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@GraphVertex(label = "User")
public class User {
    private Object id;
    private UUID userId;
    public String name;
    public String email;

    @GraphEdge(label = "following", direction = Direction.OUT)
    public List<User> friends;
}