package com.colobu.rpcx.rpc.annotation;

import java.lang.annotation.*;


/**
 * Created by goodjava@qq.com.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Documented
public @interface Consumer {

    String impl();

}
