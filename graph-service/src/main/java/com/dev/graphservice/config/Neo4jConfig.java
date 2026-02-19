package com.dev.graphservice.config;
import org.neo4j.driver.Driver;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;

@Configuration
@ConditionalOnProperty(name = "graph.impl", havingValue = "neo4j")
public class Neo4jConfig {
    @Bean
    ApplicationRunner verifyNeo4jConnectivity(Driver driver) {
        return args -> driver.verifyConnectivity();
    }
}
