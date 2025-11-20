package com.dev.graphservice.model;

import com.dev.graphservice.annotation.GraphEdge;
import com.dev.graphservice.annotation.GraphVertex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.tinkerpop.gremlin.structure.Direction;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@GraphVertex(label = "User")
public class User {
    private Object id;
    public String name;
    public String email;
    public int age;

    @GraphEdge(label = "FRIEND_OF", direction = Direction.OUT)
    public List<User> friends;
}