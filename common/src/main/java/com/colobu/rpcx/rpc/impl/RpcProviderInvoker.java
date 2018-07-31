package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.protocol.MessageType;
import com.colobu.rpcx.protocol.RemotingCommand;
import com.colobu.rpcx.rpc.*;
import com.colobu.rpcx.rpc.annotation.Provider;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class RpcProviderInvoker<T> implements Invoker<T> {

    private boolean useSpring;

    private Function<Class, Object> getBean;

    private URL url;

    private Class _interface;

    public RpcProviderInvoker(boolean useSpring, Function<Class, Object> getBean, RpcInvocation invocation) {
        this.useSpring = useSpring;
        this.getBean = getBean;
        Map<String, String> parameters = new HashMap<>();
        Class clazz = getClass0(invocation);
        this._interface = clazz;
        Provider provider = (Provider) clazz.getAnnotation(Provider.class);
        parameters.put("token", provider.token());

        url = new URL("", "", 0, parameters);
    }

    @Override
    public Class<T> getInterface() {
        return _interface;
    }


    private Class getClass0(RpcInvocation invocation) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(invocation.getClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clazz;
    }

    @Override
    public Result invoke(RpcInvocation invocation) throws RpcException {
        Object obj = null;
        RemotingCommand res = RemotingCommand.createResponseCommand();
        Result rpcResult = new RpcResult();
        try {
            String method = invocation.getMethodName();
            Class<?> clazz = getClass0(invocation);
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

        } catch (Throwable throwable) {
            ((RpcResult) rpcResult).setThrowable(throwable);
            return rpcResult;
        }
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void destroy() {

    }
}
