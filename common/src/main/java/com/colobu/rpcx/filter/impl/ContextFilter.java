package com.colobu.rpcx.filter.impl;


import com.colobu.rpcx.common.ClassUtils;
import com.colobu.rpcx.common.NetUtils;
import com.colobu.rpcx.common.StringUtils;
import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.Filter;
import com.colobu.rpcx.rpc.*;
import com.colobu.rpcx.rpc.annotation.Provider;
import com.colobu.rpcx.rpc.annotation.RpcFilter;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author goodjava@qq.com
 */
@RpcFilter(order = -2000, group = {Constants.PROVIDER})
public class ContextFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ContextFilter.class);


    @Override
    public Result invoke(Invoker<?> invoker, RpcInvocation invocation) throws RpcException {
        logger.info("ContextFilter begin className:{} methodName:{}", invocation.getClassName(), invocation.getMethodName());

        Class clazz = ClassUtils.getClassByName(invocation.getClassName());
        invoker.setInterface(clazz);
        Provider typeProvider = (Provider) clazz.getAnnotation(Provider.class);


        URL url = invocation.getUrl().copy();
        url.setHost(NetUtils.getLocalHost());
        url.setPort(0);
        invoker.setUrl(url);

        Map<String, String> parameters = url.getParameters();
        parameters.put(Constants.SIDE_KEY, Constants.PROVIDER_SIDE);

        setTypeParameters(typeProvider, parameters);

        String methodName = invocation.getMethodName();

        Method method = this.getMethod(invocation.getClassName(), methodName, invocation.getParameterTypeNames());
        invoker.setMethod(method);
        Provider methodProvider = method.getAnnotation(Provider.class);

        //方法级别的会覆盖type级别的
        if (null != methodProvider) {
            setMethodParameters(methodProvider, parameters);
        }

        Map<String, String> attachments = invocation.getAttachments();
        if (attachments != null) {
            attachments = new HashMap<>(attachments);
            attachments.remove(Constants.PATH_KEY);
            attachments.remove(Constants.GROUP_KEY);
            attachments.remove(Constants.VERSION_KEY);
            attachments.remove(Constants.TOKEN_KEY);
            attachments.remove(Constants.TIMEOUT_KEY);
        }
        RpcContext.getContext()
                .setInvoker(invoker)
                .setInvocation(invocation)
                .setAttachments(attachments)
                .setRemoteAddress(invocation.getUrl().getAddress(), invocation.getUrl().getPort())
                .setLocalAddress(invoker.getUrl().getHost(),
                        invoker.getUrl().getPort());
        invocation.setInvoker(invoker);
        try {
            return invoker.invoke(invocation);
        } finally {
            RpcContext.removeContext();
            logger.info("ContextFilter end");
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
}