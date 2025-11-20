package com.dev.gremlin_ogm.core;

import org.apache.tinkerpop.gremlin.structure.Direction;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GremlinGraphRepository {

    <T> T save(T entity);

    <T> List<T> saveAll(List<T> entities);

    <T> Optional<T> findById(Class<T> type, Object id);

    <T> List<T> findAll(Class<T> type);

    <T> List<T> findAll(Class<T> type, int limit, int offset);

    <T> Optional<T> findByProperty(Class<T> type, String key, Object value);

    <T> List<T> findByProperties(Class<T> type, Map<String, Object> properties);

    <T> boolean exists(Class<T> type, Object id);

    <T> boolean existsByProperty(Class<T> type, String key, Object value);

    <T> long countVertices(Class<T> type);

    <T> void deleteByProperty(Class<T> type, String key, Object value);

    void delete(Object id);

    <T> void deleteAll(Class<T> type);

    void deleteAll(String label);

    Map<String, Object> save(String label, Map<String, Object> properties);

    <T> T saveOrUpdate(T entity, Object id);

    Optional<Map<Object, Object>> findById(Object id);

    List<Map<Object, Object>> findAll(String label);

    List<Map<Object, Object>> findAll(String label, int limit, int offset);

    void createEdgeBetween(Object from, Object to, String label, Map<String, Object> edgeProps);

    void createEdgesBetween(Object from, List<Object> toList, String label, Direction direction);

    void deleteEdge(Object fromId, Object toId, String edgeLabel);

    void deleteAllEdges(Object vertexId, String edgeLabel);

    void deleteOutgoingEdges(Object vertexId, String edgeLabel);

    void deleteIncomingEdges(Object vertexId, String edgeLabel);

    void deleteEdgesByLabel(String edgeLabel);

    List<Map<Object, Object>> traverse(Object from, String edgeLabel);

    <T> List<T> traverseOutgoing(Class<T> type, Object from, String edgeLabel);

    <T> List<T> traverseIncoming(Class<T> type, Object to, String edgeLabel);

    <T> List<T> traverseBoth(Class<T> type, Object vertex, String edgeLabel);

    <T> List<T> traverseWithDepth(Class<T> type, Object from, String edgeLabel, int depth);

    List<List<Object>> getPath(Object from, Object to, String edgeLabel, int maxDepth);

    long countEdges(Object vertexId, String edgeLabel);

    long countAll();
}
