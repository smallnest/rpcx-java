package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.RpcException;
import com.colobu.rpcx.rpc.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * @author goodjava@qq.com
 */
public class RpcProviderInvoker<T> implements Invoker<T> {

    private static final Logger logger = LoggerFactory.getLogger(RpcProviderInvoker.class);

    /**
     * 如果是基于ioc容器的,需要提供获取bean的function
     */
    private Function<Class, Object> getBeanFunc;

    private URL url;

    private Class clazz;

    private Method method;

    public RpcProviderInvoker(Function<Class, Object> getBeanFunc) {
        this.getBeanFunc = getBeanFunc;
    }

    @Override
    public Class<T> getInterface() {
        return clazz;
    }


    @Override
    public Result invoke(RpcInvocation invocation) throws RpcException {
        Object obj = null;
        Result rpcResult = new RpcResult();
        try {
            //使用容器
            if (null != this.getBeanFunc) {
                Object b = getBeanFunc.apply(clazz);
                obj = this.method.invoke(b, invocation.getArguments());
            } else {//不使用容器
                obj = this.method.invoke(clazz.newInstance(), invocation.getArguments());
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
        return true;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void setMethod(Method method) {
        this.method = method;
    }

    @Override
    public void setInterface(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public void setUrl(URL url) {
        this.url = url;
    }
}
