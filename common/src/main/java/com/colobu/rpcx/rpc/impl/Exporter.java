package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.common.ClassUtils;
import com.colobu.rpcx.common.Config;
import com.colobu.rpcx.common.NetUtils;
import com.colobu.rpcx.common.StringUtils;
import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.FilterWrapper;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.URL;
import com.colobu.rpcx.rpc.annotation.Provider;
import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author goodjava@qq.com
 */
public class Exporter {

    private static final Logger logger = LoggerFactory.getLogger(Exporter.class);

    public static final Map<String, Invoker<Object>> invokerMap = new HashMap<>(20);

    private final Function<Class, Object> getBeanFunc;

    private final String addr;

    public Exporter(Function<Class, Object> getBeanFunc, String addr) {
        this.getBeanFunc = getBeanFunc;
        this.addr = addr;
    }


    /**
     * 导出所有对外服务(包括 echo 和 泛化调用)
     *
     * @param providerPackage
     * @return
     */
    public Set<String> export(String providerPackage) {
        logger.info("export");
        String[] ss = addr.split(":");
        final String host = ss[0];
        final int port = Integer.valueOf(ss[1]);
        Set<Class<?>> set = new ProviderFinder(providerPackage).find();
        return set.stream().map(it -> {
            String name = it.getName();

            Method[] methods = it.getDeclaredMethods();
            Arrays.stream(methods).forEach(m -> {
                String className = it.getName();
                String method = m.getName();
                //reflectasm 生成的方法需要过滤掉
                if (!method.startsWith("access$")) {
                    String[] parameterTypeNames = ClassUtils.getMethodParameterNames(m);
                    String key = ClassUtils.getMethodKey(className, method, parameterTypeNames);
                    logger.info("export key:{}", key);
                    URL url = new URL("rpcx", host, port, key);
                    //暴露出invoker,使得远程可以调用
                    initProviderInvoker(key, className, method, parameterTypeNames, url);
                }
            });
            //echo invoker
            initEchoInvoker();
            //generic invoker
            initGenericInvoker();

            //这里的url是注册到zk
            URL url = new URL("rpcx", host, port);
            url.setPath(name);
            //类名称
            Provider provider = it.getAnnotation(Provider.class);
            //抽取出权重
            String weight = provider.weight();
            String weightValue = Config.ins().get(weight);
            if (null == weightValue) {
                weightValue = "1";
            }
            String group = "";
            if (null != provider.group()) {
                group = provider.group();
            }
            //导出的时候需要确定权重
            url = url.addParameter("weight", weightValue);
            url = url.addParameter("group", group);
            return url.toFullString();
        }).collect(Collectors.toSet());
    }


    /**
     * 初始化providerInvoker
     */
    private Invoker<Object> initProviderInvoker(String key, String className, String methodName, String[] parameterTypeNames, URL url) {
        Invoker<Object> wrapperInvoker;
        Invoker<Object> invoker = new RpcProviderInvoker<>(getBeanFunc);
        Class clazz = ClassUtils.getClassByName(className);
        invoker.setInterface(clazz);

        invoker.setUrl(url);

        //类级别的注解
        Provider typeProvider = invoker.getInterface().getAnnotation(Provider.class);
        setTypeParameters(typeProvider, url.getParameters());
        Set<String> excludeFilters = Arrays.stream(typeProvider.excludeFilters()).collect(Collectors.toSet());

        //兼容性更好些
        Method method = ClassUtils.getMethod(className, methodName, parameterTypeNames);
        invoker.setMethod(method);

        //reflectasm更快些(实际上是把反射生成了直接调用的代码)
        MethodAccess methodAccess = MethodAccess.get(ClassUtils.getClassByName(className));
        invoker.setMethodAccess(methodAccess);

        //方法级别的注解
        Provider methodProvider = method.getAnnotation(Provider.class);

        //方法级别的会覆盖type级别的
        if (null != methodProvider) {
            setMethodParameters(methodProvider, url.getParameters());
        }

        wrapperInvoker = FilterWrapper.ins().buildInvokerChain(invoker, "", Constants.PROVIDER, excludeFilters);

        invokerMap.putIfAbsent(key, wrapperInvoker);
        return wrapperInvoker;
    }

    /**
     * echo
     */
    private void initEchoInvoker() {
        Invoker<Object> invoker = new RpcProviderInvoker<>(getBeanFunc);
        URL url = new URL("rpcx", "", 0);
        url.setHost(NetUtils.getLocalHost());
        url.setPort(0);
        invoker.setUrl(url);
        Invoker<Object> wrapperInvoker = FilterWrapper.ins().buildInvokerChain(invoker, "", Constants.PROVIDER, Sets.newHashSet());
        invokerMap.putIfAbsent(Constants.$ECHO, wrapperInvoker);
    }

    /**
     * 泛化调用
     */
    private void initGenericInvoker() {
        Invoker<Object> invoker = new RpcProviderInvoker<>(getBeanFunc);
        URL url = new URL("rpcx", "", 0);
        url.setHost(NetUtils.getLocalHost());
        url.setPort(0);
        invoker.setUrl(url);
        ((RpcProviderInvoker<Object>) invoker).setShare(true);
        Invoker<Object> wrapperInvoker = FilterWrapper.ins().buildInvokerChain(invoker, "", Constants.PROVIDER, Sets.newHashSet());
        invokerMap.putIfAbsent(Constants.$INVOKE, wrapperInvoker);
    }

    private void setTypeParameters(Provider provider, Map<String, String> parameters) {
        parameters.put(Constants.SIDE_KEY, Constants.PROVIDER_SIDE);
        parameters.put(Constants.TOKEN_KEY, provider.token());
        parameters.put(Constants.TIMEOUT_KEY, String.valueOf(provider.timeout()));
        parameters.put(Constants.CACHE_KEY, String.valueOf(provider.cache()));
        parameters.put(Constants.TPS_LIMIT_RATE_KEY, String.valueOf(provider.tps()));
        parameters.put(Constants.MONITOR_KEY, String.valueOf(provider.monitor()));
        parameters.put(Constants.GROUP_KEY, String.valueOf(provider.group()));
        parameters.put(Constants.VERSION_KEY, String.valueOf(provider.version()));
    }

    private void setMethodParameters(Provider provider, Map<String, String> parameters) {
        //使用了那些注解,实际的含义就是启用了那些filter
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


}
