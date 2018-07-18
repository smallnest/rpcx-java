package com.colobu.rpcx.common.retry;


import java.util.function.Function;

public interface RetryPolicy {

    boolean retry(Function<Integer, Boolean> func);
}
