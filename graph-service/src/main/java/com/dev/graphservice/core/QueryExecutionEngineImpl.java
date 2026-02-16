package com.dev.graphservice.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class QueryExecutionEngineImpl implements QueryExecutionEngine {

    @Override
    public Object save(GraphTraversalSource g, Object obj) {
        if (!(obj instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Expected a Map for map-based storage");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> props = (Map<String, Object>) map;
        String label = (String) map.get("label");
        props.remove("label");
        var t = g.addV(label);
        props.forEach(t::property);
        return t.id().next();
    }

    @Override
    public Optional<Map<Object, Object>> findById(GraphTraversalSource g, Object id) {
        return g.V(id).elementMap().tryNext();
    }

    @Override
    public List<Map<Object, Object>> findAll(GraphTraversalSource g, String label) {
        return g.V().hasLabel(label).elementMap().toList();
    }

    @Override
    public List<Map<Object, Object>> findAll(GraphTraversalSource g, String label, int limit, int offset) {
        return g.V().hasLabel(label).elementMap().range(offset, offset + limit).toList();
    }

    @Override
    public Optional<Map<Object, Object>> findByProperty(GraphTraversalSource g, String label, String key, Object value) {
        return g.V().hasLabel(label).has(key, value).elementMap().tryNext();
    }

    @Override
    public List<Map<Object, Object>> findByProperties(GraphTraversalSource g, String label, Map<String, Object> properties) {
        var t = g.V().hasLabel(label);
        for (var e : properties.entrySet()) t = t.has(e.getKey(), e.getValue());
        return t.elementMap().toList();
    }

    @Override
    public boolean exists(GraphTraversalSource g, Object id) {
        return g.V(id).hasNext();
    }

    @Override
    public boolean existsByProperty(GraphTraversalSource g, String label, String key, Object value) {
        return g.V().hasLabel(label).has(key, value).hasNext();
    }

    @Override
    public long countVertices(GraphTraversalSource g, String label) {
        return g.V().hasLabel(label).count().next();
    }

    @Override
    public long countAll(GraphTraversalSource g) {
        return g.V().count().next();
    }

    @Override
    public void link(GraphTraversalSource g, String fromLabel, Map<String, Object> fromProps,
                     String toLabel, Map<String, Object> toProps, String edgeLabel, Map<String, Object> edgeProps) {
        Vertex fromV = getOrCreateVertex(g, fromLabel, fromProps);
        Vertex toV = getOrCreateVertex(g, toLabel, toProps);

        var edge = g.V(fromV.id()).as("a").V(toV.id()).addE(edgeLabel).from("a");
        if (edgeProps != null) edgeProps.forEach(edge::property);
        Object edgeId = edge.id().next();
        log.info("Edge created with id: {}", edgeId);
    }

    @Override
    public void addEdgeBetween(GraphTraversalSource g, Object fromId, Object toId, String edgeLabel, Direction direction) {
        switch (direction) {
            case OUT -> {
                var edge = g.V(fromId).as("a").V(toId).addE(edgeLabel).from("a");
                edge.iterate();
            }
            case IN -> {
                var edge = g.V(toId).as("a").V(fromId).addE(edgeLabel).from("a");
                edge.iterate();
            }
            case BOTH -> {
                addEdgeBetween(g, fromId, toId, edgeLabel, Direction.OUT);
                addEdgeBetween(g, fromId, toId, edgeLabel, Direction.IN);
            }
            default -> throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }

    @Override
    public void addEdgesBetween(GraphTraversalSource g, Object fromId, List<Object> toIdList, String edgeLabel) {
        for (Object toId : toIdList) {
            addEdgeBetween(g, fromId, toId, edgeLabel, Direction.OUT);
        }
        log.info("Linked vertex {} to {} targets with edge label: {}", fromId, toIdList.size(), edgeLabel);
    }

    private Vertex getOrCreateVertex(GraphTraversalSource g, String label, Map<String, Object> props) {
        var t = g.V().hasLabel(label);
        for (var e : props.entrySet()) t = t.has(e.getKey(), e.getValue());
        return t.tryNext().orElseGet(() -> {
            var add = g.addV(label);
            props.forEach(add::property);
            return add.next();
        });
    }

    @Override
    public List<Map<Object, Object>> traverse(GraphTraversalSource g, String label, Map<String, Object> props, String edgeLabel) {
        var v = g.V().hasLabel(label);
        for (var e : props.entrySet()) v = v.has(e.getKey(), e.getValue());
        return v.out(edgeLabel).elementMap().toList();
    }

    @Override
    public List<Map<Object, Object>> traverseOutgoing(GraphTraversalSource g, Object from, String edgeLabel) {
        if (from instanceof Long || from instanceof String) {
            return g.V(from).out(edgeLabel).elementMap().toList();
        }
        return List.of();
    }

    @Override
    public List<Map<Object, Object>> traverseIncoming(GraphTraversalSource g, Object to, String edgeLabel) {
        if (to instanceof Long || to instanceof String) {
            return g.V(to).in(edgeLabel).elementMap().toList();
        }
        return List.of();
    }

    @Override
    public List<Map<Object, Object>> traverseBoth(GraphTraversalSource g, Object vertexId, String edgeLabel) {
        if (vertexId instanceof Long || vertexId instanceof String) {
            return g.V(vertexId).both(edgeLabel).elementMap().toList();
        }
        return List.of();
    }

    @Override
    public List<Map<Object, Object>> traverseWithDepth(GraphTraversalSource g, Object from, String edgeLabel, int depth) {
        if (!(from instanceof Long || from instanceof String)) return List.of();
        return g.V(from).repeat(__.out(edgeLabel)).times(depth).elementMap().toList();
    }

    @Override
    public List<List<Object>> getPath(GraphTraversalSource g, Object from, Object to, String edgeLabel, int maxDepth) {
        if (!(from instanceof Long || from instanceof String) || !(to instanceof Long || to instanceof String)) {
            return List.of();
        }
        var paths = g.V(from).repeat(__.out(edgeLabel)).times(maxDepth).until(__.hasId(to)).path().toList();
        List<List<Object>> result = new ArrayList<>();
        for (var path : paths) {
            result.add(path.objects());
        }
        return result;
    }

    @Override
    public long countEdges(GraphTraversalSource g, Object vertexId, String edgeLabel) {
        return g.V(vertexId).bothE(edgeLabel).count().next();
    }

    @Override
    public void delete(GraphTraversalSource g, Object id) {
        g.V(id).drop().iterate();
    }

    @Override
    public void deleteAll(GraphTraversalSource g, String label) {
        g.V().hasLabel(label).drop().iterate();
    }

    @Override
    public void deleteByProperty(GraphTraversalSource g, String label, String key, Object value) {
        g.V().hasLabel(label).has(key, value).drop().iterate();
    }

    @Override
    public void deleteEdge(GraphTraversalSource g, Object fromId, Object toId, String edgeLabel) {
        g.V(fromId).outE(edgeLabel).where(__.inV().hasId(toId)).drop().iterate();
    }

    @Override
    public void deleteAllEdges(GraphTraversalSource g, Object vertexId, String edgeLabel) {
        g.V(vertexId).bothE(edgeLabel).drop().iterate();
    }

    @Override
    public void deleteOutgoingEdges(GraphTraversalSource g, Object vertexId, String edgeLabel) {
        g.V(vertexId).outE(edgeLabel).drop().iterate();
    }

    @Override
    public void deleteIncomingEdges(GraphTraversalSource g, Object vertexId, String edgeLabel) {
        g.V(vertexId).inE(edgeLabel).drop().iterate();
    }

    @Override
    public void deleteEdgesByLabel(GraphTraversalSource g, String edgeLabel) {
        g.E().hasLabel(edgeLabel).drop().iterate();
    }
}
