package com.pizzaria.interfaces;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    String action() default "";
    String entity() default "";
}
