package com.colobu.rpcx.rpc.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Documented
public @interface Provider {

    String impl();

}
