package com.colobu.rpcx.rpc.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Documented
public @interface Provider {

    String name() default "";

    String version() default "0.0.1";

    /**
     * 服务启动token验证,客户端需要提供相同的token
     * @return
     */
    String token() default "";

    /**
     * 服务器超时调用后会打印警告,不影响正常调用
     * @return
     */
    long timeout() default 0;

    /**
     * 调用缓存
     * @return
     */
    boolean cache() default false;

}
