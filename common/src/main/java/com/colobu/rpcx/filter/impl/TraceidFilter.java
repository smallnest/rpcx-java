package com.colobu.rpcx.filter.impl;

import com.colobu.rpcx.common.ClassUtils;
import com.colobu.rpcx.common.StringUtils;
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

import java.util.UUID;

@RpcFilter(order = -999, group = {Constants.PROVIDER})
public class TraceidFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(TraceidFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, RpcInvocation invocation) throws RpcException {
        String clientTraceId = invocation.getAttachment(Constants.TRACE_ID);
        String key = ClassUtils.getMethodKey(invocation.getClassName(), invocation.getMethodName(), invocation.getParameterTypeNames());
        String traceId = UUID.randomUUID().toString();
        if (StringUtils.isNotEmpty(clientTraceId)) {
            traceId = clientTraceId + "," + traceId;
        }
        logger.info("invoke:{} traceId:{}", key, traceId);
        RpcContext.getContext().getAttachments().put(Constants.TRACE_ID, traceId);
        return invoker.invoke(invocation);
    }
}
