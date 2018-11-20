package com.colobu.rpcx.filter.impl;

import com.colobu.rpcx.common.ClassUtils;
import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.Filter;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.RpcException;
import com.colobu.rpcx.rpc.annotation.RpcFilter;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author goodjava@qq.com
 * 打印traceid
 */
@RpcFilter(group = {Constants.CONSUMER}, order = -1000)
public class ConsumerTraceidFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerTraceidFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, RpcInvocation invocation) throws RpcException {
        Result result = invoker.invoke(invocation);

        if (null != result) {
            String key = ClassUtils.getMethodKey(invocation.getClassName(), invocation.getMethodName(), invocation.getParameterTypeNames());
            String traceId = result.getAttachment(Constants.TRACE_ID);
            String spanId = result.getAttachment(Constants.SPAN_ID);
            logger.info("invoke {} traceid:{} spanId:{} error:{}", key, traceId, spanId, result.hasException());
        }

        return result;
    }
}
