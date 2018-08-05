package com.colobu.rpcx.common.retry;


import com.colobu.rpcx.rpc.Result;

import java.util.function.Function;

/**
 * @author goodjava@qq.com
 */
public interface RetryPolicy {

    /**
     * 判断的条件是result中没有异常
     * @param func
     * @return
     */
    Result retry(Function<Integer, Result> func);
}
