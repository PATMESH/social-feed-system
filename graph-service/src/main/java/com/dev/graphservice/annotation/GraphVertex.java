package com.dev.gremlin_ogm.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GraphVertex {
    String label() default "";
    boolean immutable() default false;
}
