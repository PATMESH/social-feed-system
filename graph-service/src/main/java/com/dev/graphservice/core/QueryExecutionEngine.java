package com.dev.graphservice.core;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface QueryExecutionEngine {

    Object save(GraphTraversalSource g, Object entityOrMap);

    Optional<Map<Object, Object>> findById(GraphTraversalSource g, Object id);

    List<Map<Object, Object>> findAll(GraphTraversalSource g, String label);

    List<Map<Object, Object>> findAll(GraphTraversalSource g, String label, int limit, int offset);

    Optional<Map<Object, Object>> findByProperty(GraphTraversalSource g, String label, String key, Object value);

    List<Map<Object, Object>> findByProperties(GraphTraversalSource g, String label, Map<String, Object> properties);

    boolean exists(GraphTraversalSource g, Object id);

    boolean existsByProperty(GraphTraversalSource g, String label, String key, Object value);

    long countVertices(GraphTraversalSource g, String label);

    long countAll(GraphTraversalSource g);

    Object link(GraphTraversalSource g, String fromLabel, Map<String, Object> fromProps,
                String toLabel, Map<String, Object> toProps, String edgeLabel, Map<String, Object> edgeProps);

    void addEdgeBetween(GraphTraversalSource g, Object fromId, Object toId, String edgeLabel, Direction direction);

    void addEdgesBetween(GraphTraversalSource g, Object fromId, List<Object> toIdList, String edgeLabel);

    List<Map<Object, Object>> traverse(GraphTraversalSource g, String label, Map<String, Object> props, String edgeLabel);

    List<Map<Object, Object>> traverseOutgoing(GraphTraversalSource g, Object from, String edgeLabel);

    List<Map<Object, Object>> traverseIncoming(GraphTraversalSource g, Object to, String edgeLabel);

    List<Map<Object, Object>> traverseBoth(GraphTraversalSource g, Object vertexId, String edgeLabel);

    List<Map<Object, Object>> traverseWithDepth(GraphTraversalSource g, Object from, String edgeLabel, int depth);

    List<List<Object>> getPath(GraphTraversalSource g, Object from, Object to, String edgeLabel, int maxDepth);

    long countEdges(GraphTraversalSource g, Object vertexId, String edgeLabel);

    void delete(GraphTraversalSource g, Object id);

    void deleteAll(GraphTraversalSource g, String label);

    void deleteByProperty(GraphTraversalSource g, String label, String key, Object value);

    void deleteEdge(GraphTraversalSource g, Object fromId, Object toId, String edgeLabel);

    void deleteAllEdges(GraphTraversalSource g, Object vertexId, String edgeLabel);

    void deleteOutgoingEdges(GraphTraversalSource g, Object vertexId, String edgeLabel);

    void deleteIncomingEdges(GraphTraversalSource g, Object vertexId, String edgeLabel);

    void deleteEdgesByLabel(GraphTraversalSource g, String edgeLabel);
}
