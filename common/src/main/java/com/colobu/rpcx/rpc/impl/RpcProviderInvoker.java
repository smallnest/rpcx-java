package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.protocol.MessageType;
import com.colobu.rpcx.protocol.RemotingCommand;
import com.colobu.rpcx.rpc.*;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.stream.Stream;

public class RpcProviderInvoker<T> implements Invoker<T> {

    private boolean useSpring;

    private Function<Class, Object> getBean;


    public RpcProviderInvoker(boolean useSpring, Function<Class, Object> getBean) {
        this.useSpring = useSpring;
        this.getBean = getBean;
    }

    @Override
    public Class<T> getInterface() {
        return null;
    }

    @Override
    public Result invoke(RpcInvocation invocation) throws RpcException {
        Object obj = null;
        RemotingCommand res = RemotingCommand.createResponseCommand();
        Result rpcResult = new RpcResult();
        try {
            String method = invocation.getMethodName();
            Class<?> clazz = Class.forName(invocation.getClassName());
            Class[] clazzArray = Stream.of(invocation.getParameterTypeNames()).map(it -> {
                try {
                    return ReflectUtils.name2class(ReflectUtils.desc2name(it));
                } catch (ClassNotFoundException e) {
                    throw new RpcException(e);
                }
            }).toArray(Class[]::new);
            Method m = clazz.getMethod(method, clazzArray);
            if (useSpring) {//使用spring容器
                Object b = getBean.apply(clazz);
                obj = m.invoke(b, invocation.getArguments());
            } else {//不使用容器
                obj = m.invoke(clazz.newInstance(), invocation.getArguments());
            }

            Message resMessage = new Message();
            resMessage.servicePath = invocation.servicePath;
            resMessage.serviceMethod = invocation.serviceMethod;

            resMessage.setMessageType(MessageType.Response);
            resMessage.setSeq(invocation.opaque);
            resMessage.payload = HessianUtils.write(obj);
            res.setMessage(resMessage);
            ((RpcResult) rpcResult).setValue(res);
            return rpcResult;

        }catch (Throwable throwable) {
            ((RpcResult) rpcResult).setThrowable(throwable);
            return rpcResult;
        }
    }

    @Override
    public URL getUrl() {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void destroy() {

    }
}
