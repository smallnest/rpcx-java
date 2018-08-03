package com.colobu.rpcx.rpc.annotation;


import java.lang.annotation.*;


/**
 * Created by goodjava@qq.com.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Documented
public @interface RpcFilter {

    //数字越小的越先执行
    int order() default 0;

    String[] group() default {};

}
