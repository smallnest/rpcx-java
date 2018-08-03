package com.colobu.rpcx.common.retry;


import java.util.function.Function;

/**
 * Created by goodjava@qq.com.
 */
public interface RetryPolicy {

    boolean retry(Function<Integer, Boolean> func);
}
