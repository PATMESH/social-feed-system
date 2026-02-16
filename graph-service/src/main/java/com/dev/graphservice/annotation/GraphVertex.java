package com.dev.graphservice.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GraphVertex {
    String label() default "";
    boolean immutable() default false;
}
