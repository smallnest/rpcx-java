package com.colobu.rpcx.tps;


import com.colobu.rpcx.rpc.Invocation;
import com.colobu.rpcx.rpc.URL;

public interface TPSLimiter {

    /**
     * 根据 tps 限流规则判断是否限制此次调用.
     *
     * @param url url
     * @param invocation invocation
     * @return true 则允许调用，否则不允许
     */
    boolean isAllowable(URL url, Invocation invocation);

}
