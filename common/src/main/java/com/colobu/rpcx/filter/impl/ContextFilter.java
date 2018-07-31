package com.colobu.rpcx.filter.impl;


import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.Filter;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.RpcContext;
import com.colobu.rpcx.rpc.RpcException;
import com.colobu.rpcx.rpc.annotation.RpcFilter;
import com.colobu.rpcx.rpc.impl.RpcInvocation;

import java.util.HashMap;
import java.util.Map;

/**
 * ContextInvokerFilter
 */
@RpcFilter
public class ContextFilter implements Filter {

    public Result invoke(Invoker<?> invoker, RpcInvocation invocation) throws RpcException {
        Map<String, String> attachments = invocation.getAttachments();
        if (attachments != null) {
            attachments = new HashMap<>(attachments);
            attachments.remove(Constants.PATH_KEY);
            attachments.remove(Constants.GROUP_KEY);
            attachments.remove(Constants.VERSION_KEY);
            attachments.remove(Constants.TOKEN_KEY);
            attachments.remove(Constants.TIMEOUT_KEY);
        }
        RpcContext.getContext()
                .setInvoker(invoker)
                .setInvocation(invocation)
                .setAttachments(attachments)
                .setLocalAddress(invoker.getUrl().getHost(),
                        invoker.getUrl().getPort());
        if (invocation instanceof RpcInvocation) {
            invocation.setInvoker(invoker);
        }
        try {
            return invoker.invoke(invocation);
        } finally {
            RpcContext.removeContext();
        }
    }
}