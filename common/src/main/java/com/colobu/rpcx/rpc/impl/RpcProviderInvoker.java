package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.common.ClassUtils;
import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.rpc.*;
import com.esotericsoftware.reflectasm.MethodAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * @author goodjava@qq.com
 */
public class RpcProviderInvoker<T> implements Invoker<T> {

    private static final Logger logger = LoggerFactory.getLogger(RpcProviderInvoker.class);

    private static final boolean useMethodAccess = true;

    /**
     * 这个invoker 是否被共享(泛化调用就是共享的)
     * 如果是共享的 则class 和 method 都是不能设值的,不然并发下是有问题的
     */
    private boolean share = false;

    /**
     * 如果是基于ioc容器的,需要提供获取bean的function
     */
    private Function<Class, Object> getBeanFunc;

    private URL url;

    private Class clazz;

    private Method method;

    private MethodAccess methodAccess;

    public RpcProviderInvoker(Function<Class, Object> getBeanFunc) {
        this.getBeanFunc = getBeanFunc;
    }

    @Override
    public Class<T> getInterface() {
        return clazz;
    }


    @Override
    public Result invoke(RpcInvocation invocation) {
        Result rpcResult = new RpcResult();
        String traceId = "";
        try {
            Object obj = null;
            traceId = RpcContext.getContext().getAttachments().get(Constants.TRACE_ID);
            //设置traceId
            rpcResult.getAttachments().put(Constants.TRACE_ID, traceId == null ? "" : traceId);
            //使用容器
            if (null != this.getBeanFunc) {
                if (share) {
                    //泛化调用,每次都创建不同的
                    Class c = ClassUtils.getClassByName(invocation.getClassName());
                    Object b = getBeanFunc.apply(c);
                    Method method = ClassUtils.getMethod(invocation.getClassName(), invocation.getMethodName(), invocation.getParameterTypeNames());
                    obj = method.invoke(b, invocation.getArguments());
                } else {
                    //每次都调用相同的,应为class 和 method 都已经实例化好了
                    Object b = getBeanFunc.apply(clazz);
                    if (useMethodAccess) {
                        obj = methodAccess.invoke(b, invocation.getMethodName(), invocation.getArguments());
                    } else {
                        obj = this.method.invoke(b, invocation.getArguments());
                    }
                }
            } else {//不使用容器
                if (share) {
                    Class c = ClassUtils.getClassByName(invocation.getClassName());
                    Object b = getBeanFunc.apply(c);
                    Method method = ClassUtils.getMethod(invocation.getClassName(), invocation.getMethodName(), invocation.getParameterTypeNames());
                    obj = method.invoke(b, invocation.getArguments());
                } else {
                    if (useMethodAccess) {
                        obj = methodAccess.invoke(clazz.newInstance(), invocation.getMethodName(), invocation.getArguments());
                    } else {
                        obj = this.method.invoke(clazz.newInstance(), invocation.getArguments());
                    }
                }
            }
            rpcResult.setValue(obj);
            return rpcResult;
        } catch (Throwable throwable) {
            String message = throwable.getMessage();
            if (throwable.getCause() != null) {
                message = throwable.getCause().getMessage();
            }
            message = "traceId:" + traceId + ", message:" + message;
            RpcException ex = new RpcException(message, throwable, "-2");
            rpcResult.setThrowable(ex);
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
    public Method getMethod() {
        return this.method;
    }

    @Override
    public void setInterface(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    public void setMethodAccess(MethodAccess methodAccess) {
        this.methodAccess = methodAccess;
    }

    public void setShare(boolean share) {
        this.share = share;
    }
}
