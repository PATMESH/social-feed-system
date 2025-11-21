package com.dev.graphservice.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OGMProcessor {

    private final EntityMapper mapper;
    private final QueryExecutionEngine queryExecution;

    public Object saveEntity(GraphTraversalSource g, Object entity) {
        Map<String, Object> props = mapper.extractProperties(entity);
        String label = mapper.resolveLabel(entity.getClass());
        props.put("label", label);

        Object vertexId = queryExecution.save(g, props);

        List<EntityMapper.EdgeRelation> edges = mapper.extractEdges(entity);
        for (EntityMapper.EdgeRelation edge : edges) {
            Object targetId = saveEntity(g, edge.target());
            queryExecution.addEdgeBetween(g, vertexId, targetId, edge.label(), edge.direction());
        }
        return vertexId;
    }

    //TODO: Need to implement unique id annotation to upsert based on that
    public void saveOrUpdate(GraphTraversalSource g, Object entity, Object id) {
    }

    public <T> List<T> saveAll(GraphTraversalSource g, List<T> entities) {
        List<T> saved = new ArrayList<>();
        for (T entity : entities) {
            saveEntity(g, entity);
            saved.add(entity);
        }
        return saved;
    }

    public <T> Optional<T> findById(GraphTraversalSource g, Class<T> type, Object id) {
        return queryExecution.findById(g, id)
                .map(m -> mapper.populateFromMap( m, type));
    }

    public <T> List<T> findAll(GraphTraversalSource g, Class<T> type) {
        List<Map<Object, Object>> maps = queryExecution.findAll(g, mapper.resolveLabel(type));
        List<T> entities = new ArrayList<>();
        for (Map<Object, Object> map : maps) {
            entities.add(mapper.populateFromMap(map, type));
        }
        return entities;
    }

    public <T> List<T> findAll(GraphTraversalSource g, Class<T> type, int limit, int offset) {
        String label = mapper.resolveLabel(type);
        List<Map<Object, Object>> maps = queryExecution.findAll(g, label, limit, offset);
        List<T> entities = new ArrayList<>();
        for (Map<Object, Object> map : maps) {
            entities.add(mapper.populateFromMap(map, type));
        }
        return entities;
    }

    public <T> Optional<T> findByProperty(GraphTraversalSource g, Class<T> type, String key, Object value) {
        String label = mapper.resolveLabel(type);
        Optional<Map<Object, Object>> map = queryExecution.findByProperty(g, label, key, value);
        return map.map(m -> mapper.populateFromMap(m, type));
    }

    public <T> List<T> findByProperties(GraphTraversalSource g, Class<T> type, Map<String, Object> properties) {
        String label = mapper.resolveLabel(type);
        List<Map<Object, Object>> maps = queryExecution.findByProperties(g, label, properties);
        List<T> entities = new ArrayList<>();
        for (Map<Object, Object> map : maps) {
            entities.add(mapper.populateFromMap(map, type));
        }
        return entities;
    }

    public <T> boolean exists(GraphTraversalSource g, Class<T> type, Object id) {
        return queryExecution.exists(g, id);
    }

    public <T> boolean existsByProperty(GraphTraversalSource g, Class<T> type, String key, Object value) {
        String label = mapper.resolveLabel(type);
        return queryExecution.existsByProperty(g, label, key, value);
    }

    public <T> long countVertices(GraphTraversalSource g, Class<T> type) {
        String label = mapper.resolveLabel(type);
        return queryExecution.countVertices(g, label);
    }

    public void link(GraphTraversalSource g, Object from, Object to, String label, Map<String, Object> edgeProps) {
        String fromLabel = mapper.resolveLabel(from.getClass());
        String toLabel = mapper.resolveLabel(to.getClass());
        Map<String, Object> fromProps = mapper.extractProperties(from);
        Map<String, Object> toProps = mapper.extractProperties(to);
        queryExecution.link(g, fromLabel, fromProps, toLabel, toProps, label, edgeProps);
    }

    public List<Map<Object, Object>> traverse(GraphTraversalSource g, Object from, String edgeLabel) {
        String label = mapper.resolveLabel(from.getClass());
        Map<String, Object> props = mapper.extractProperties(from);
        return queryExecution.traverse(g, label, props, edgeLabel);
    }

    public <T> List<T> traverseOutgoing(GraphTraversalSource g, Class<T> type, Object from, String edgeLabel) {
        List<Map<Object, Object>> maps = queryExecution.traverseOutgoing(g, from, edgeLabel);
        List<T> result = new ArrayList<>();
        for (Map<Object, Object> map : maps) {
            result.add(mapper.populateFromMap(map, type));
        }
        return result;
    }

    public <T> List<T> traverseIncoming(GraphTraversalSource g, Class<T> type, Object to, String edgeLabel) {
        List<Map<Object, Object>> maps = queryExecution.traverseIncoming(g, to, edgeLabel);
        List<T> result = new ArrayList<>();
        for (Map<Object, Object> map : maps) {
            result.add(mapper.populateFromMap(map, type));
        }
        return result;
    }

    public <T> List<T> traverseBoth(GraphTraversalSource g, Class<T> type, Object vertexId, String edgeLabel) {
        List<Map<Object, Object>> maps = queryExecution.traverseBoth(g, vertexId, edgeLabel);
        List<T> result = new ArrayList<>();
        for (Map<Object, Object> map : maps) {
            result.add(mapper.populateFromMap(map, type));
        }
        return result;
    }

    public <T> List<T> traverseWithDepth(GraphTraversalSource g, Class<T> type, Object from, String edgeLabel, int depth) {
        List<Map<Object, Object>> maps = queryExecution.traverseWithDepth(g, from, edgeLabel, depth);
        List<T> result = new ArrayList<>();
        for (Map<Object, Object> map : maps) {
            result.add(mapper.populateFromMap(map, type));
        }
        return result;
    }

    public <T> void deleteAll(GraphTraversalSource g, Class<T> type) {
        String label = mapper.resolveLabel(type);
        queryExecution.deleteAll(g, label);
    }

    public <T> void deleteByProperty(GraphTraversalSource g, Class<T> type, String key, Object value) {
        String label = mapper.resolveLabel(type);
        queryExecution.deleteByProperty(g, label, key, value);
    }
}
