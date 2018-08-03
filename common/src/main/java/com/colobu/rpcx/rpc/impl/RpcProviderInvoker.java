package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.common.ClassUtils;
import com.colobu.rpcx.common.NetUtils;
import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.rpc.*;
import com.colobu.rpcx.rpc.annotation.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by goodjava@qq.com.
 */
public class RpcProviderInvoker<T> implements Invoker<T> {

    private static final Logger logger = LoggerFactory.getLogger(RpcProviderInvoker.class);

    //如果是基于ioc容器的,需要提供获取bean 的function
    private Function<Class, Object> getBeanFunc;

    private URL url;

    private Class _interface;

    public RpcProviderInvoker(Function<Class, Object> getBeanFunc, RpcInvocation invocation) {
        this.getBeanFunc = getBeanFunc;

        Class clazz = ClassUtils.getClassByName(invocation.getClassName());
        this._interface = clazz;
        Provider provider = (Provider) clazz.getAnnotation(Provider.class);


        this.url = invocation.getUrl().copy();
        this.url.setHost(NetUtils.getLocalHost());
        this.url.setPort(0);
        Map<String, String> parameters = this.url.getParameters();
        parameters.put(Constants.TOKEN_KEY, provider.token());
        parameters.put(Constants.TIMEOUT_KEY, String.valueOf(provider.timeout()));
        parameters.put(Constants.CACHE_KEY, String.valueOf(provider.cache()));
        parameters.put(Constants.TPS_LIMIT_RATE_KEY, String.valueOf(provider.tps()));
        parameters.put(Constants.MONITOR_KEY, String.valueOf(provider.monitor()));
        parameters.put(Constants.SIDE_KEY, Constants.PROVIDER_SIDE);
        parameters.put(Constants.GROUP_KEY, String.valueOf(provider.group()));
        parameters.put(Constants.VERSION_KEY, String.valueOf(provider.version()));
    }

    @Override
    public Class<T> getInterface() {
        return _interface;
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

            if (null != this.getBeanFunc) {//使用容器
                Object b = getBeanFunc.apply(clazz);
                obj = m.invoke(b, invocation.getArguments());
            } else {//不使用容器
                obj = m.invoke(clazz.newInstance(), invocation.getArguments());
            }
            rpcResult.setValue(obj);
            return rpcResult;

        } catch (Throwable throwable) {
            logger.error(throwable.getMessage(), throwable);
            rpcResult.setThrowable(throwable);
            return rpcResult;
        }
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void destroy() {

    }
}
