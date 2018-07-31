package com.colobu.rpcx.rpc.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Documented
public @interface Provider {

    String name() default "";

    String version() default "0.0.1";

    String token() default "";

}
