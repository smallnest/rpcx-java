package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.common.ClassUtils;
import com.colobu.rpcx.common.NetUtils;
import com.colobu.rpcx.common.StringUtils;
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
 * @author goodjava@qq.com
 */
public class RpcProviderInvoker<T> implements Invoker<T> {

    private static final Logger logger = LoggerFactory.getLogger(RpcProviderInvoker.class);

    /**
     * 如果是基于ioc容器的,需要提供获取bean 的function
     */
    private Function<Class, Object> getBeanFunc;

    private URL url;

    private Class _interface;

    private Method method;

    public RpcProviderInvoker(Function<Class, Object> getBeanFunc, RpcInvocation invocation) {
        this.getBeanFunc = getBeanFunc;

        Class clazz = ClassUtils.getClassByName(invocation.getClassName());
        this._interface = clazz;
        Provider typeProvider = (Provider) clazz.getAnnotation(Provider.class);

        String methodName = invocation.getMethodName();
        if (methodName.equals(Constants.$INVOKE)) {
            methodName = invocation.getArguments()[0].toString();
        }

        this.method = this.getMethod(invocation.getClassName(), methodName, invocation.getParameterTypeNames());
        Provider methodProvider = this.method.getAnnotation(Provider.class);


        this.url = invocation.getUrl().copy();
        this.url.setHost(NetUtils.getLocalHost());
        this.url.setPort(0);

        Map<String, String> parameters = this.url.getParameters();
        parameters.put(Constants.SIDE_KEY, Constants.PROVIDER_SIDE);

        setTypeParameters(typeProvider, parameters);
        //方法级别的会覆盖type级别的
        if (null != methodProvider) {
            setMethodParameters(methodProvider, parameters);
        }
    }

    private void setTypeParameters(Provider provider, Map<String, String> parameters) {
        parameters.put(Constants.TOKEN_KEY, provider.token());
        parameters.put(Constants.TIMEOUT_KEY, String.valueOf(provider.timeout()));
        parameters.put(Constants.CACHE_KEY, String.valueOf(provider.cache()));
        parameters.put(Constants.TPS_LIMIT_RATE_KEY, String.valueOf(provider.tps()));
        parameters.put(Constants.MONITOR_KEY, String.valueOf(provider.monitor()));
        parameters.put(Constants.GROUP_KEY, String.valueOf(provider.group()));
        parameters.put(Constants.VERSION_KEY, String.valueOf(provider.version()));
    }

    private void setMethodParameters(Provider provider, Map<String, String> parameters) {
        if (StringUtils.isNotEmpty(provider.token())) {
            parameters.put(Constants.TOKEN_KEY, provider.token());
        }
        if (-1 != provider.timeout()) {
            parameters.put(Constants.TIMEOUT_KEY, String.valueOf(provider.timeout()));
        }
        if (false != provider.cache()) {
            parameters.put(Constants.CACHE_KEY, String.valueOf(provider.cache()));
        }
        if (-1 != provider.tps()) {
            parameters.put(Constants.TPS_LIMIT_RATE_KEY, String.valueOf(provider.tps()));
        }
        if (false != provider.monitor()) {
            parameters.put(Constants.MONITOR_KEY, String.valueOf(provider.monitor()));
        }
        if (StringUtils.isNotEmpty(provider.group())) {
            parameters.put(Constants.GROUP_KEY, String.valueOf(provider.group()));
        }
        if (StringUtils.isNotEmpty(provider.version())) {
            parameters.put(Constants.VERSION_KEY, String.valueOf(provider.version()));
        }
    }


    @Override
    public Class<T> getInterface() {
        return _interface;
    }


    private Method getMethod(String className, String methodName, String[] parameterTypeNames) {
        Class<?> clazz = ClassUtils.getClassByName(className);
        Class[] clazzArray = Stream.of(parameterTypeNames).map(it -> ReflectUtils.forName(it)).toArray(Class[]::new);
        Method m = null;
        try {
            m = clazz.getMethod(methodName, clazzArray);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return m;
    }


    @Override
    public Result invoke(RpcInvocation invocation) throws RpcException {
        Object obj = null;
        Result rpcResult = new RpcResult();
        try {
            Class<?> clazz = ClassUtils.getClassByName(invocation.getClassName());
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
}
