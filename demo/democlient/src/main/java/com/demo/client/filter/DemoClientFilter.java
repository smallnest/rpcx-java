package com.demo.client.filter;


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
 */
@RpcFilter(group = {Constants.CONSUMER})
public class DemoClientFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(DemoClientFilter.class);

    public Result invoke(Invoker<?> invoker, RpcInvocation invocation) throws RpcException {
        logger.info("DemoClientFilter invoke");
        return invoker.invoke(invocation);
    }
}
