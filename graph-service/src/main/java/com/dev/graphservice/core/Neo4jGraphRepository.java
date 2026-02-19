package com.dev.graphservice.core;

import com.dev.graphservice.enums.Direction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;


@Repository
@ConditionalOnProperty(name = "graph.impl", havingValue = "neo4j")
@RequiredArgsConstructor
@Slf4j
public class Neo4jGraphRepository implements GraphRepository {

    private final Neo4jClient neo4jClient;
    private final Neo4jTemplate neo4jTemplate;

    @Override
    public <T> T save(T entity) {
        return neo4jTemplate.save(entity);
    }

    @Override
    public <T> List<T> saveAll(List<T> entities) {
        return neo4jTemplate.saveAll(entities);
    }

    @Override
    public <T> Optional<T> findById(Class<T> type, Object id) {
        return neo4jTemplate.findById(id, type);
    }

    @Override
    public <T> List<T> findAll(Class<T> type) {
        return neo4jTemplate.findAll(type);
    }

    @Override
    public <T> List<T> findAll(Class<T> type, int limit, int offset) {
         return neo4jTemplate.findAll(type).stream().skip(offset).limit(limit).collect(Collectors.toList());
    }

    @Override
    public <T> Optional<T> findByProperty(Class<T> type, String key, Object value) {
        String cypher = String.format("MATCH (n:%s) WHERE n.%s = $value RETURN n LIMIT 1", type.getSimpleName(), key);
        return neo4jTemplate.findOne(cypher, Collections.singletonMap("value", convertValue(value)), type);
    }

    @Override
    public <T> List<T> findByProperties(Class<T> type, Map<String, Object> properties) {
        String label = type.getSimpleName();
        StringBuilder cypher = new StringBuilder(String.format("MATCH (n:`%s`) WHERE ", label));
        List<String> conditions = new ArrayList<>();
        for (String key : properties.keySet()) {
            conditions.add(String.format("n.%s = $%s", key, key));
        }
        cypher.append(String.join(" AND ", conditions));
        cypher.append(" RETURN n");

        return neo4jClient.query(cypher.toString())
                .bindAll(properties)
                .fetchAs(type)
                .mappedBy((t, r) -> neo4jTemplate.findById(r.get("n").asNode().elementId(), type).orElse(null))
                .all().stream().filter(Objects::nonNull).toList();
    }

    @Override
    public List<List<Object>> getPath(Object from, Object to, String edgeLabel, int maxDepth) {
        String cypher = String.format("MATCH p=(a)-[:`%s`*1..%d]->(b) WHERE elementId(a) = $from AND elementId(b) = $to RETURN p", edgeLabel, maxDepth);
        return neo4jClient.query(cypher)
                .bind(from).to("from")
                .bind(to).to("to")
                .fetch()
                .all()
                .stream()
                .map(m -> {
                   org.neo4j.driver.types.Path path = (org.neo4j.driver.types.Path) m.get("p");
                   List<Object> objects = new ArrayList<>();
                   path.forEach(segment -> {
                       objects.add(segment.start().asMap());
                       objects.add(segment.relationship().asMap());
                   });
                   objects.add(path.end().asMap());
                   return objects;
                })
                .collect(Collectors.toList());
    }

    @Override
    public <T> boolean exists(Class<T> type, Object id) {
        return neo4jTemplate.findById(id, type).isPresent();
    }

    @Override
    public <T> boolean existsByProperty(Class<T> type, String key, Object value) {
        return findByProperty(type, key, value).isPresent();
    }

    @Override
    public <T> long countVertices(Class<T> type) {
        return neo4jTemplate.count(type);
    }

    @Override
    public <T> void deleteByProperty(Class<T> type, String key, Object value) {
         String label = type.getSimpleName(); 
         String cypher = String.format("MATCH (n:`%s`) WHERE n.%s = $value DETACH DELETE n", label, key);
         neo4jClient.query(cypher).bind(value).to("value").run();
    }

    @Override
    public void delete(Object id) {
        neo4jClient.query("MATCH (n) WHERE elementId(n) = $id DETACH DELETE n")
                .bind(id).to("id")
                .run();
    }

    @Override
    public <T> void deleteAll(Class<T> type) {
        neo4jTemplate.deleteAll(type);
    }

    @Override
    public void deleteAll(String label) {
         neo4jClient.query(String.format("MATCH (n:`%s`) DETACH DELETE n", label)).run();
    }

    @Override
    public Map<String, Object> save(String label, Map<String, Object> properties) {
        String props = properties.keySet().stream()
                .map(k -> String.format("%s: $%s", k, k))
                .collect(Collectors.joining(", "));
        String cypher = String.format("CREATE (n:`%s` {%s}) RETURN n", label, props);
        
        Optional<Map<String, Object>> result = neo4jClient.query(cypher)
                .bindAll(properties)
                .fetch()
                .one();
        
        return result.orElse(Map.of());
    }

    @Override
    public <T> T saveOrUpdate(T entity, Object id) {
        return neo4jTemplate.save(entity);
    }

    @Override
    public Optional<Map<Object, Object>> findById(Object id) {
        return neo4jClient.query("MATCH (n) WHERE elementId(n) = $id RETURN n")
                .bind(id).to("id")
                .fetch()
                .one()
                .map(m -> (Map<Object, Object>)(Map)m.get("n")); 
    }

    @Override
    public List<Map<Object, Object>> findAll(String label) {
        return neo4jClient.query(String.format("MATCH (n:`%s`) RETURN n", label))
                .fetch()
                .all()
                .stream()
                .map(m -> (Map<Object,Object>)(Map)m.get("n"))
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<Object, Object>> findAll(String label, int limit, int offset) {
         return neo4jClient.query(String.format("MATCH (n:`%s`) RETURN n SKIP $skip LIMIT $limit", label))
                .bind(offset).to("skip")
                .bind(limit).to("limit")
                .fetch()
                .all()
                .stream()
                .map(m -> (Map<Object,Object>)(Map)m.get("n"))
                .collect(Collectors.toList());
    }

    @Override
    public void createEdgeBetween(Object from, Object to, String label, Map<String, Object> edgeProps) {
        // Similar to below but with props
         String cypher = String.format(
            "MATCH (a), (b) WHERE elementId(a) = $from AND elementId(b) = $to " +
            "MERGE (a)-[r:`%s`]->(b) SET r += $props", label);
         
         neo4jClient.query(cypher)
            .bind(from).to("from")
            .bind(to).to("to")
            .bind(edgeProps).to("props")
            .run();
    }

    @Override
    public void createEdgeBetween(Object from, Object to, String label, Direction direction) {

        String cypher;

        if (direction == Direction.OUT) {
            cypher = String.format("""
            MATCH (a)
            WHERE id(a) = $from
            MATCH (b)
            WHERE id(b) = $to
            MERGE (a)-[r:`%s`]->(b)
        """, label);
        } else {
            cypher = String.format("""
            MATCH (a)
            WHERE id(a) = $from
            MATCH (b)
            WHERE id(b) = $to
            MERGE (b)-[r:`%s`]->(a)
        """, label);
        }

        neo4jClient.query(cypher)
                .bind(from).to("from")
                .bind(to).to("to")
                .run();
    }

    @Override
    public void createEdgesBetween(Object from, List<Object> toList, String label, Direction direction) {
         toList.forEach(to -> createEdgeBetween(from, to, label, direction));
    }

    @Override
    public void deleteEdge(Object fromId, Object toId, String edgeLabel) {
         String cypher = String.format(
            "MATCH (a)-[r:`%s`]->(b) WHERE elementId(a) = $from AND elementId(b) = $to DELETE r", edgeLabel);
          neo4jClient.query(cypher)
            .bind(fromId).to("from")
            .bind(toId).to("to")
            .run();
    }

    @Override
    public void deleteAllEdges(Object vertexId, String edgeLabel) {
        String cypher = String.format(
            "MATCH (a)-[r:`%s`]-() WHERE elementId(a) = $id DELETE r", edgeLabel);
         neo4jClient.query(cypher)
                .bind(vertexId).to("id")
                .run();
    }

    @Override
    public void deleteOutgoingEdges(Object vertexId, String edgeLabel) {
         String cypher = String.format(
            "MATCH (a)-[r:`%s`]->() WHERE elementId(a) = $id DELETE r", edgeLabel);
         neo4jClient.query(cypher)
                .bind(vertexId).to("id")
                .run();
    }

    @Override
    public void deleteIncomingEdges(Object vertexId, String edgeLabel) {
            String cypher = String.format(
            "MATCH (a)<-[r:`%s`]-() WHERE elementId(a) = $id DELETE r", edgeLabel);
         neo4jClient.query(cypher)
                .bind(vertexId).to("id")
                .run();
    }

    @Override
    public void deleteEdgesByLabel(String edgeLabel) {
         String cypher = String.format("MATCH ()-[r:`%s`]-() DELETE r", edgeLabel);
         neo4jClient.query(cypher).run();
    }

    @Override
    public List<Map<Object, Object>> traverse(Object from, String edgeLabel) {
        // Return raw nodes
        String cypher = String.format(
                "MATCH (a)-[:`%s`]-(b) WHERE elementId(a) = $id RETURN b", edgeLabel);
          return neo4jClient.query(cypher)
                .bind(from).to("id")
                .fetch()
                .all()
                .stream()
                .map(m -> (Map<Object,Object>)(Map)m.get("b"))
                .collect(Collectors.toList());
    }

    @Override
    public <T> List<T> traverseOutgoing(Class<T> type, Object from, String edgeLabel) {
        String cypher = String.format("MATCH (a)-[:`%s`]->(b) WHERE id(a) = $id RETURN b", edgeLabel);
        return neo4jTemplate.findAll(cypher, Collections.singletonMap("id", from), type);
    }

    @Override
    public <T> List<T> traverseIncoming(Class<T> type, Object to, String edgeLabel) {
           String cypher = String.format("MATCH (a)<-[:`%s`]-(b) WHERE id(a) = $id RETURN b", edgeLabel);
        return neo4jTemplate.findAll(cypher, Collections.singletonMap("id", to), type);
    }

    @Override
    public <T> List<T> traverseBoth(Class<T> type, Object vertex, String edgeLabel) {
           String cypher = String.format(
                "MATCH (a)-[:`%s`]-(b) WHERE elementId(a) = $id RETURN b", edgeLabel);
          return (List<T>) neo4jClient.query(cypher)
                  .bind(vertex).to("id")
                  .fetchAs(type)
                   .mappedBy((typeSystem, record) -> neo4jTemplate.findById(record.get("b").asNode().elementId(), type).orElse(null))
                  .all().stream().filter(Objects::nonNull).toList();
    }

    @Override
    public <T> List<T> traverseWithDepth(Class<T> type, Object from, String edgeLabel, int depth) {
          String cypher = String.format(
                "MATCH (a)-[:`%s`*1..%d]->(b) WHERE elementId(a) = $id RETURN b", edgeLabel, depth);
           return (List<T>) neo4jClient.query(cypher)
                  .bind(from).to("id")
                  .fetchAs(type)
                   .mappedBy((typeSystem, record) -> neo4jTemplate.findById(record.get("b").asNode().elementId(), type).orElse(null))
                  .all().stream().filter(Objects::nonNull).toList();
    }



    @Override
    public long countEdges(Object vertexId, String edgeLabel) {
         String cypher = String.format(
                "MATCH (a)-[r:`%s`]->() WHERE elementId(a) = $id RETURN count(r)", edgeLabel);
         return neo4jClient.query(cypher)
                 .bind(vertexId).to("id")
                 .fetchAs(Long.class)
                 .one().orElse(0L);
    }

    @Override
    public long countAll() {
        return neo4jClient.query("MATCH (n) RETURN count(n)")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
    }

    private Object convertValue(Object value) {
        if (value instanceof UUID uuid) {
            return uuid.toString();
        }
        return value;
    }

}
