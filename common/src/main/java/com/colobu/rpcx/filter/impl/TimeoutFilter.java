package com.colobu.rpcx.filter.impl;


import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.Filter;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.RpcException;
import com.colobu.rpcx.rpc.annotation.RpcFilter;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


/**
 * @author goodjava@qq.com
 */
//@RpcFilter(order = -1000, group = {Constants.PROVIDER})
public class TimeoutFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(TimeoutFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, RpcInvocation invocation) throws RpcException {
        if (!"-1".equals(invoker.getUrl().getParameter(Constants.TIMEOUT_KEY))) {
            logger.debug("TimeoutFilter begin");
            long start = System.currentTimeMillis();
            Result result = invoker.invoke(invocation);
            long elapsed = System.currentTimeMillis() - start;

            int v = invoker.getUrl().getParameter(Constants.TIMEOUT_KEY, Integer.MAX_VALUE);
            if (invoker.getUrl() != null && v != 0 && elapsed > v) {
                logger.warn("invoke time out. method: " + invocation.getMethodName()
                        + "arguments: " + Arrays.toString(invocation.getArguments()) + " , url is "
                        + invoker.getUrl() + ", invoke elapsed " + elapsed + " ms.");
            }
            logger.debug("TimeoutFilter end");
            return result;
        }

        return invoker.invoke(invocation);
    }

}