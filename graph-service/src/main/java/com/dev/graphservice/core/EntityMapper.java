package com.dev.graphservice.core;

import com.dev.graphservice.annotation.GraphEdge;
import com.dev.graphservice.annotation.GraphVertex;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.springframework.stereotype.Component;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class EntityMapper {

    private final Map<Class<?>, String> labelCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Field[]> fieldCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Method[]> methodCache = new ConcurrentHashMap<>();

    public String resolveLabel(Class<?> entityClass) {
        return labelCache.computeIfAbsent(entityClass, clazz -> {
            GraphVertex annotation = clazz.getAnnotation(GraphVertex.class);
            if (annotation != null && !annotation.label().isEmpty()) {
                return annotation.label();
            }
            return clazz.getSimpleName();
        });
    }

    public Map<String, Object> extractProperties(Object entity) {
        if (entity == null) return Map.of();
        Map<String, Object> props = new LinkedHashMap<>();
        Field[] fields = getCachedFields(entity.getClass());
        for (Field field : fields) {
            if (shouldExtractField(field)) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(entity);
                    String propertyName = field.getName();
                    if (value != null) props.put(propertyName, value);
                } catch (IllegalAccessException e) {
                    log.warn("Cannot access field: {}", field.getName(), e);
                }
            }
        }
        return props;
    }

    public List<EdgeRelation> extractEdges(Object entity) {
        List<EdgeRelation> list = new ArrayList<>();
        for (Field f : entity.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(GraphEdge.class)) {
                GraphEdge edge = f.getAnnotation(GraphEdge.class);
                f.setAccessible(true);
                try {
                    Object val = f.get(entity);
                    Direction direction = switch (edge.direction()) {
                        case IN -> Direction.IN;
                        case OUT -> Direction.OUT;
                        case BOTH -> Direction.BOTH;
                    };
                    
                    if (val instanceof Collection<?> col) {
                        for (Object o : col) list.add(new EdgeRelation(edge.label(), o, direction));
                    } else if (val != null) {
                        list.add(new EdgeRelation(edge.label(), val, direction));
                    }
                } catch (IllegalAccessException ignored) {}
            }
        }
        return list;
    }

    public static <T> T populateFromVertex(org.apache.tinkerpop.gremlin.structure.Vertex v, Class<T> type) {
        try {
            T instance = type.getDeclaredConstructor().newInstance();
            for (Field f : type.getDeclaredFields()) {
                if (f.isAnnotationPresent(GraphEdge.class)) continue;
                f.setAccessible(true);
                Object value = v.property(f.getName()).isPresent() ? v.property(f.getName()).value() : null;
                if (value != null) f.set(instance, value);
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T populateFromMap(Map<Object, Object> map, Class<T> type) {
        try {
            T instance = type.getDeclaredConstructor().newInstance();
            for (Field f : type.getDeclaredFields()) {
                if (f.isAnnotationPresent(GraphEdge.class)) continue;
                f.setAccessible(true);
                if (f.getName().equals("id")) {
                    Object idValue = map.get(org.apache.tinkerpop.gremlin.structure.T.id);
                    if (idValue != null) {
                        if (f.getType() == String.class) {
                            f.set(instance, idValue.toString());
                        } else if (f.getType() == Long.class || f.getType() == long.class) {
                            f.set(instance, Long.valueOf(idValue.toString()));
                        } else {
                            f.set(instance, idValue);
                        }
                    }
                } else {
                    Object value = map.get(f.getName());
                    if (value != null) f.set(instance, value);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isEdgeField(Field field) {
        return field.getAnnotation(GraphEdge.class) != null;
    }

    private Field[] getCachedFields(Class<?> clazz) {
        return fieldCache.computeIfAbsent(clazz, Class::getDeclaredFields);
    }

    private boolean shouldExtractField(Field field) {
        return !field.getName().startsWith("$")
                && !field.getName().startsWith("_")
                && !field.isSynthetic()
                && !Modifier.isStatic(field.getModifiers())
                && !isEdgeField(field)
                && !Modifier.isTransient(field.getModifiers());
                //&& field.getAnnotation(GraphIgnore.class) == null;
    }


public record EdgeRelation(String label, Object target, Direction direction) {}
}
