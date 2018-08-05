package com.colobu.rpcx.filter.impl;


import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.Filter;
import com.colobu.rpcx.rpc.*;
import com.colobu.rpcx.rpc.annotation.RpcFilter;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import com.colobu.rpcx.rpc.impl.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author goodjava@qq.com
 */
@RpcFilter(group = {Constants.PROVIDER}, order = -1999)
public class ExceptionFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, RpcInvocation invocation) throws RpcException {
        try {
            Result result = invoker.invoke(invocation);
            return result;
        } catch (RpcException ex) {
            Result result = new RpcResult();
            result.setThrowable(ex);
            return result;
        } catch (Throwable throwable) {
            Result result = new RpcResult();
            result.setThrowable(throwable);
            return result;
        }
    }

}