package com.colobu.rpcx.filter.impl;


import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.Filter;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.RpcException;
import com.colobu.rpcx.rpc.annotation.RpcFilter;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import com.colobu.rpcx.tps.DefaultTPSLimiter;
import com.colobu.rpcx.tps.TPSLimiter;

/**
 * Created by goodjava@qq.com.
 */
@RpcFilter(group = {Constants.PROVIDER})
public class TpsLimitFilter implements Filter {

    private static final TPSLimiter tpsLimiter = new DefaultTPSLimiter();

    @Override
    public Result invoke(Invoker<?> invoker, RpcInvocation invocation) throws RpcException {
        if (invocation.getUrl().getParameter(Constants.TPS_LIMIT_RATE_KEY,"-1").equals("-1")) {
            return invoker.invoke(invocation);
        }

        if (!tpsLimiter.isAllowable(invoker.getUrl(), invocation)) {
            throw new RpcException(
                    new StringBuilder(64)
                            .append("Failed to invoke service ")
                            .append(invoker.getInterface().getName())
                            .append(".")
                            .append(invocation.getMethodName())
                            .append(" because exceed max service tps.")
                            .toString());
        }

        return invoker.invoke(invocation);
    }

}
