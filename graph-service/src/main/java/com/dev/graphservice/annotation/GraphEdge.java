package com.dev.graphservice.annotation;

import org.apache.tinkerpop.gremlin.structure.Direction;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GraphEdge {
    String label();
    Direction direction() default Direction.OUT;
    boolean lazy() default false;
}
