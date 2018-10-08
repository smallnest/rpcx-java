package com.colobu.rpcx.filter.impl;


import com.colobu.rpcx.common.ClassUtils;
import com.colobu.rpcx.common.NetUtils;
import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.Filter;
import com.colobu.rpcx.protocol.LanguageCode;
import com.colobu.rpcx.rpc.*;
import com.colobu.rpcx.rpc.annotation.RpcFilter;
import com.colobu.rpcx.rpc.impl.RpcInvocation;


/**
 * @author goodjava@qq.com
 * 初始化consumer 上下文
 */
@RpcFilter(group = {Constants.CONSUMER}, order = -3000)
public class ConsumerContextFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, RpcInvocation invocation) throws RpcException {
        String path = "";

        if (invocation.getLanguageCode().equals(LanguageCode.JAVA)) {
            path = ClassUtils.getMethodKey(invocation.getClassName(), invocation.getMethodName(), invocation.getParameterTypeNames());
        }
        URL url = new URL("rpcx", NetUtils.getLocalAddress().getHostAddress(), 0, path);
        url = url.setServiceInterface(invocation.getClassName() + "." + invocation.getMethodName());
        invoker.setUrl(url);
        RpcContext.getContext()
                .setLocalAddress(NetUtils.getLocalHost(), 0)
                .setRemoteAddress(invoker.getUrl().getHost(), invoker.getUrl().getPort());
        invocation.setInvoker(invoker);
        try {
            return invoker.invoke(invocation);
        } finally {
            //同步的和oneway的直接清除掉context
            if (invocation.getSendType().equals(Constants.SYNC_KEY) || invocation.getSendType().equals(Constants.ONE_WAY_KEY)) {
                RpcContext.removeContext();
            } else {
                //async 需要手动自己清理(RpcContext.removeContext),因为future 还要从 contex他中拿
                RpcContext.getContext().clearAttachments();
                RpcContext.getContext().setServiceAddr("");
                RpcContext.getContext().setLocalAddress("", 0);
                RpcContext.getContext().setRemoteAddress("", 0);
            }
        }
    }

}