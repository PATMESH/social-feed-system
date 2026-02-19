package com.dev.graphservice.model;

import com.dev.graphservice.annotation.GraphEdge;
import com.dev.graphservice.annotation.GraphVertex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.dev.graphservice.enums.Direction;

import java.util.List;
import java.util.UUID;

import com.dev.graphservice.enums.Direction;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@GraphVertex(label = "User")
@Node("User")
public class User {
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Property("userId")
    private UUID userId;
    public String name;
    public String email;

    @GraphEdge(label = "following", direction = Direction.OUT)
    @Relationship(type = "following", direction = Relationship.Direction.OUTGOING)
    public List<User> friends;
}