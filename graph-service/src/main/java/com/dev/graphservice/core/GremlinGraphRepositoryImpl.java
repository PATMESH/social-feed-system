package com.dev.graphservice.core;

import lombok.RequiredArgsConstructor;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GremlinGraphRepositoryImpl implements GremlinGraphRepository {

    private final OGMProcessor ogmProcessor;
    private final QueryExecutionEngine queryExecution;
    private final GremlinTransactionManager tx;

    @Override
    public <T> T save(T entity) {
        return tx.execute(g -> {
            ogmProcessor.saveEntity(g, entity);
            return entity;
        });
    }

    @Override
    public <T> List<T> saveAll(List<T> entities) {
        return tx.execute(g -> ogmProcessor.saveAll(g, entities));
    }

    @Override
    public Map<String, Object> save(String label, Map<String, Object> properties) {
        return tx.execute(g -> {
            properties.put("label", label);
            Object id = queryExecution.save(g, properties);
            properties.put("id", id);
            return properties;
        });
    }

    @Override
    public <T> T saveOrUpdate(T entity, Object id) {
        return tx.execute(g -> {
            ogmProcessor.saveOrUpdate(g, entity, id);
            return entity;
        });
    }

    @Override
    public <T> Optional<T> findById(Class<T> type, Object id) {
        return tx.execute(g -> ogmProcessor.findById(g, type, id));
    }

    @Override
    public <T> List<T> findAll(Class<T> type) {
        return tx.execute(g -> ogmProcessor.findAll(g, type));
    }

    @Override
    public <T> List<T> findAll(Class<T> type, int limit, int offset) {
        return tx.execute(g -> ogmProcessor.findAll(g, type, limit, offset));
    }

    @Override
    public <T> Optional<T> findByProperty(Class<T> type, String key, Object value) {
        return tx.execute(g -> ogmProcessor.findByProperty(g, type, key, value));
    }

    @Override
    public <T> List<T> findByProperties(Class<T> type, Map<String, Object> properties) {
        return tx.execute(g -> ogmProcessor.findByProperties(g, type, properties));
    }

    @Override
    public <T> boolean exists(Class<T> type, Object id) {
        return tx.execute(g -> ogmProcessor.exists(g, type, id));
    }

    @Override
    public <T> boolean existsByProperty(Class<T> type, String key, Object value) {
        return tx.execute(g -> ogmProcessor.existsByProperty(g, type, key, value));
    }

    @Override
    public <T> long countVertices(Class<T> type) {
        return tx.execute(g -> ogmProcessor.countVertices(g, type));
    }

    @Override
    public <T> void deleteByProperty(Class<T> type, String key, Object value) {
        tx.execute(g -> {
            ogmProcessor.deleteByProperty(g, type, key, value);
            return null;
        });
    }

    @Override
    public void delete(Object id) {
        tx.execute(g -> {
            queryExecution.delete(g, id);
            return null;
        });
    }

    @Override
    public <T> void deleteAll(Class<T> type) {
        tx.execute(g -> {
            ogmProcessor.deleteAll(g, type);
            return null;
        });
    }

    @Override
    public void deleteAll(String label) {
        tx.execute(g -> {
            queryExecution.deleteAll(g, label);
            return null;
        });
    }

    @Override
    public Optional<Map<Object, Object>> findById(Object id) {
        return tx.execute(g -> queryExecution.findById(g, id));
    }

    @Override
    public List<Map<Object, Object>> findAll(String label) {
        return tx.execute(g -> queryExecution.findAll(g, label));
    }

    @Override
    public List<Map<Object, Object>> findAll(String label, int limit, int offset) {
        return tx.execute(g -> queryExecution.findAll(g, label, limit, offset));
    }

    @Override
    public void createEdgeBetween(Object from, Object to, String label, Map<String, Object> edgeProps) {
        tx.execute(g -> {
            ogmProcessor.link(g, from, to, label, edgeProps);
            return null;
        });
    }

    @Override
    public void createEdgesBetween(Object from, List<Object> toList, String label,  Direction direction) {
        tx.execute(g -> {
            queryExecution.addEdgeBetween(g, from, toList, label, direction);
            return null;
        });
    }

    @Override
    public void deleteEdge(Object fromId, Object toId, String edgeLabel) {
        tx.execute(g -> {
            queryExecution.deleteEdge(g, fromId, toId, edgeLabel);
            return null;
        });
    }

    @Override
    public void deleteAllEdges(Object vertexId, String edgeLabel) {
        tx.execute(g -> {
            queryExecution.deleteAllEdges(g, vertexId, edgeLabel);
            return null;
        });
    }

    @Override
    public void deleteOutgoingEdges(Object vertexId, String edgeLabel) {
        tx.execute(g -> {
            queryExecution.deleteOutgoingEdges(g, vertexId, edgeLabel);
            return null;
        });
    }

    @Override
    public void deleteIncomingEdges(Object vertexId, String edgeLabel) {
        tx.execute(g -> {
            queryExecution.deleteIncomingEdges(g, vertexId, edgeLabel);
            return null;
        });
    }

    @Override
    public void deleteEdgesByLabel(String edgeLabel) {
        tx.execute(g -> {
            queryExecution.deleteEdgesByLabel(g, edgeLabel);
            return null;
        });
    }

    @Override
    public List<Map<Object, Object>> traverse(Object from, String edgeLabel) {
        return tx.execute(g -> ogmProcessor.traverse(g, from, edgeLabel));
    }

    @Override
    public <T> List<T> traverseOutgoing(Class<T> type, Object from, String edgeLabel) {
        return tx.execute(g -> ogmProcessor.traverseOutgoing(g, type, from, edgeLabel));
    }

    @Override
    public <T> List<T> traverseIncoming(Class<T> type, Object to, String edgeLabel) {
        return tx.execute(g -> ogmProcessor.traverseIncoming(g, type, to, edgeLabel));
    }

    @Override
    public <T> List<T> traverseBoth(Class<T> type, Object vertexId, String edgeLabel) {
        return tx.execute(g -> ogmProcessor.traverseBoth(g, type, vertexId, edgeLabel));
    }

    @Override
    public <T> List<T> traverseWithDepth(Class<T> type, Object from, String edgeLabel, int depth) {
        return tx.execute(g -> ogmProcessor.traverseWithDepth(g, type, from, edgeLabel, depth));
    }

    @Override
    public List<List<Object>> getPath(Object from, Object to, String edgeLabel, int maxDepth) {
        return tx.execute(g -> queryExecution.getPath(g, from, to, edgeLabel, maxDepth));
    }

    @Override
    public long countEdges(Object vertexId, String edgeLabel) {
        return tx.execute(g -> queryExecution.countEdges(g, vertexId, edgeLabel));
    }

    @Override
    public long countAll() {
        return tx.execute(queryExecution::countAll);
    }
}
