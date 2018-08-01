package com.colobu.rpcx.rpc.impl;


import com.colobu.rpcx.common.NetUtils;
import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.Filter;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.RpcContext;
import com.colobu.rpcx.rpc.RpcException;
import com.colobu.rpcx.rpc.annotation.RpcFilter;

/**
 * ConsumerContextInvokerFilter
 */
@RpcFilter(group = {Constants.CONSUMER})
public class ConsumerContextFilter implements Filter {

    public Result invoke(Invoker<?> invoker, RpcInvocation invocation) throws RpcException {
        RpcContext.getContext()
                .setInvoker(invoker)
                .setInvocation(invocation)
                .setLocalAddress(NetUtils.getLocalHost(), 0)
                .setRemoteAddress(invoker.getUrl().getHost(),
                        invoker.getUrl().getPort());
        invocation.setInvoker(invoker);
        try {
            return invoker.invoke(invocation);
        } finally {
            RpcContext.getContext().clearAttachments();
        }
    }

}