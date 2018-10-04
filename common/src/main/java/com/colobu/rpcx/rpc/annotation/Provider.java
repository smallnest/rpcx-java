package com.colobu.rpcx.rpc.annotation;

import java.lang.annotation.*;


/**
 * Created by goodjava@qq.com.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Documented
public @interface Provider {

    String group() default "";

    String name() default "";

    String version() default "";

    String[] excludeFilters() default "";

    /**
     * 服务启动token验证,客户端需要提供相同的token
     *
     * @return
     */
    String token() default "";

    /**
     * 服务器超时调用后会打印警告,不影响正常调用
     * 超时filter
     *
     * @return
     */
    long timeout() default -1;

    /**
     * 缓存filter
     *
     * @return
     */
    boolean cache() default false;

    /**
     * 是否开启监控
     * 监控filter
     *
     * @return
     */
    boolean monitor() default false;

    /**
     * 一分钟内tps 能达到的上限  -1 是没有上限
     * tps filter
     *
     * @return
     */
    int tps() default -1;

    /**
     * 权重
     *
     * @return
     */
    String weight() default "";

}
