package com.dev.graphservice;

import com.dev.graphservice.core.GraphRepository;
import com.dev.graphservice.core.Neo4jGraphRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = "graph.impl=neo4j")
class Neo4jContextTest {

    @Autowired
    private GraphRepository graphRepository;

    @Test
    void contextLoadsAndUsesNeo4jRepository() {
        assertThat(graphRepository).isInstanceOf(Neo4jGraphRepository.class);
    }
}
