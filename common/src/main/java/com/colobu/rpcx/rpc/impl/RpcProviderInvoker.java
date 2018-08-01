package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.common.ClassUtils;
import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.rpc.*;
import com.colobu.rpcx.rpc.annotation.Provider;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RpcProviderInvoker<T> implements Invoker<T> {

    //使用spring 容器
    private boolean useSpring;

    //如果是基于ioc容器的,需要提供获取bean 的function
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
        parameters.put(Constants.TOKEN_KEY, provider.token());
        parameters.put(Constants.TIMEOUT_KEY, String.valueOf(provider.timeout()));
        parameters.put(Constants.CACHE_KEY, String.valueOf(provider.cache()));
        parameters.put(Constants.TPS_LIMIT_RATE_KEY, String.valueOf(provider.tps()));
        parameters.put(Constants.MONITOR_KEY, String.valueOf(provider.monitor()));
        parameters.put(Constants.SIDE_KEY, Constants.PROVIDER_SIDE);


        url = new URL("rpcx", "", 0, parameters);
        url.setServiceInterface(invocation.getClassName() + "" + invocation.getMethodName());
        String params = Stream.of(invocation.getParameterTypeNames()).collect(Collectors.joining(","));
        url.setPath(invocation.getClassName() + "." + invocation.getMethodName() + "(" + params + ")");
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
        Result rpcResult = new RpcResult();
        try {
            String method = invocation.getMethodName();
            Class<?> clazz = ClassUtils.getClassByName(invocation.getClassName());
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
            ((RpcResult) rpcResult).setValue(obj);
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
