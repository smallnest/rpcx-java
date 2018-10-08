package com.colobu.rpcx.filter.impl;


import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.Filter;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.RpcContext;
import com.colobu.rpcx.rpc.RpcException;
import com.colobu.rpcx.rpc.annotation.RpcFilter;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author goodjava@qq.com
 */
@RpcFilter(order = -2000, group = {Constants.PROVIDER})
public class ContextFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ContextFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, RpcInvocation invocation) throws RpcException {
        logger.debug("ContextFilter begin className:{} methodName:{}", invocation.getClassName(), invocation.getMethodName());
        Map<String, String> attachments = invocation.getAttachments();
        RpcContext.getContext()
                .setAttachments(attachments)
                .setRemoteAddress(invocation.getUrl().getAddress(), invocation.getUrl().getPort())
                .setLocalAddress(invoker.getUrl().getHost(),
                        invoker.getUrl().getPort());
        invocation.setInvoker(invoker);
        try {
            return invoker.invoke(invocation);
        } finally {
            RpcContext.removeContext();
            logger.debug("ContextFilter end");
        }
    }


}