package com.colobu.rpcx.common.retry;

import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Created by goodjava@qq.com.
 */
public class RetryNTimes implements RetryPolicy {


    private final int n;

    public RetryNTimes(int n) {
        this.n = n;
    }

    @Override
    public boolean retry(Function<Integer, Boolean> func) {
        return IntStream.range(1, n + 1).filter(it -> func.apply(it)).findFirst().isPresent();
    }
}
