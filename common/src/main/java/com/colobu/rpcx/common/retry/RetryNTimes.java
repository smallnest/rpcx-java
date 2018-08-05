package com.colobu.rpcx.common.retry;

import com.colobu.rpcx.rpc.Result;

import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * @author goodjava@qq.com
 */
public class RetryNTimes implements RetryPolicy {


    private final int n;

    public RetryNTimes(int n) {
        this.n = n;
    }

    @Override
    public Result retry(Function<Integer, Result> func) {
        Result res = null;
        for (int i = 1; i < n + 1; i++) {
            res = func.apply(i);
            if (!res.getAttachment("needRetry").equals("true")) {
                return res;
            }
        }
        return res;
    }
}
