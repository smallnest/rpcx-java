package com.demo.filter;

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
 * Created by goodjava@qq.com.
 */
@RpcFilter(group = {Constants.PROVIDER})
public class DemoFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(DemoFilter.class);

    public Result invoke(Invoker<?> invoker, RpcInvocation invocation) throws RpcException {
        logger.info("----------->demoFilter url:{}", invocation.url.toFullString());
        return invoker.invoke(invocation);
    }
}
