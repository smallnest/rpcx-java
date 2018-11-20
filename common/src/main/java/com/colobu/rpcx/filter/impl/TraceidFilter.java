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

/**
 * @author goodjava@qq.com
 */
@RpcFilter(order = -999, group = {Constants.PROVIDER})
public class TraceidFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(TraceidFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, RpcInvocation invocation) throws RpcException {
        String clientTraceId = invocation.getAttachment(Constants.TRACE_ID);
        String key = ClassUtils.getMethodKey(invocation.getClassName(), invocation.getMethodName(), invocation.getParameterTypeNames());
        String traceId = "";
        String spanId = invocation.getAttachment(Constants.SPAN_ID, "");
        if (StringUtils.isNotEmpty(clientTraceId)) {
            traceId = clientTraceId;
            spanId = spanId + "," + UUID.randomUUID().toString();
        } else {
            //new
            traceId = UUID.randomUUID().toString();
            spanId = traceId;
        }
        logger.info("invoke begin:{} traceId:{} spanId:{}", key, traceId, spanId);
        RpcContext.getContext().getAttachments().put(Constants.TRACE_ID, traceId);
        RpcContext.getContext().getAttachments().put(Constants.SPAN_ID, spanId);
        try {
            Result res = invoker.invoke(invocation);
            logger.info("invoke end:{} success traceId:{} spanId:{}", key, traceId, spanId);
            return res;
        } catch (Throwable ex) {
            logger.info("invoke end:{} failure traceId:{} spanId:{}", key, traceId, spanId);
            throw ex;
        }
    }
}
