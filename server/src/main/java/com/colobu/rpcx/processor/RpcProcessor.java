package com.colobu.rpcx.processor;

import com.colobu.rpcx.common.ClassUtils;
import com.colobu.rpcx.common.NetUtils;
import com.colobu.rpcx.common.StringUtils;
import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.FilterWrapper;
import com.colobu.rpcx.netty.NettyRequestProcessor;
import com.colobu.rpcx.protocol.LanguageCode;
import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.protocol.MessageType;
import com.colobu.rpcx.protocol.RemotingCommand;
import com.colobu.rpcx.rpc.*;
import com.colobu.rpcx.rpc.annotation.Provider;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import com.colobu.rpcx.rpc.impl.RpcProviderInvoker;
import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author goodjava@qq.com
 * 处理远程rpc调用
 */
public class RpcProcessor implements NettyRequestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(RpcProcessor.class);

    private static final boolean useMethodAccess = true;

    private static final ConcurrentHashMap<String, Invoker<Object>> invokerMap = new ConcurrentHashMap<>();

    private final Function<Class, Object> getBeanFunc;

    public RpcProcessor(Function<Class, Object> getBeanFunc) {
        this.getBeanFunc = getBeanFunc;
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) {

        request.decode();

        String language = request.getMessage().metadata.get(Constants.LANGUAGE);
        String className = request.getMessage().servicePath;
        String methodName = request.getMessage().serviceMethod;
        RpcInvocation invocation = null;
        //golang调用是没有language的
        if (null == language) {
            invocation = new RpcInvocation();
            invocation.setLanguageCode(LanguageCode.GO);
            invocation.url = new URL("rpcx", "", 0);
            invocation.url.setPath(className + "." + methodName);
            //golang 参数是byte[]
            invocation.setParameterTypeNames(new String[]{"byte[]"});
            //参数就是payload数据
            invocation.setArguments(new Object[]{request.getMessage().payload});
        } else if (LanguageCode.JAVA.name().equals(language)) {
            invocation = (RpcInvocation) HessianUtils.read(request.getMessage().payload);
            invocation.url.setHost(request.getMessage().metadata.get("_host"));
            invocation.url.setPort(Integer.parseInt(request.getMessage().metadata.get("_port")));
        }

        invocation.setClassName(className);
        invocation.setMethodName(methodName);

        String key = ClassUtils.getMethodKey(className, methodName, invocation.getParameterTypeNames());

        Invoker<Object> wrapperInvoker = null;
        if (invokerMap.containsKey(key)) {
            wrapperInvoker = invokerMap.get(key);
        } else {
            wrapperInvoker = initProviderInvoker(key, methodName, invocation);
        }
        Result rpcResult = wrapperInvoker.invoke(invocation);

        RemotingCommand res = RemotingCommand.createResponseCommand(new Message(invocation.getClassName(), invocation.getMethodName(), MessageType.Response, request.getOpaque()));

        if (invocation.languageCode.equals(LanguageCode.HTTP)) {
            res.getMessage().payload = new Gson().toJson(rpcResult.getValue()).getBytes();
        } else if (invocation.languageCode.equals(LanguageCode.JAVA)) {
            res.getMessage().payload = HessianUtils.write(rpcResult.getValue());
        } else {
            res.getMessage().payload = (byte[]) rpcResult.getValue();
        }
        if (rpcResult.hasException()) {
            logger.error(rpcResult.getException().getMessage(), rpcResult.getException());
            res.getMessage().metadata.put(Constants.RPCX_ERROR_CODE, "-2");
            res.getMessage().metadata.put(Constants.RPCX_ERROR_MESSAGE, rpcResult.getException().getMessage());
        }
        return res;
    }

    /**
     * 初始化providerInvoker
     */
    private synchronized Invoker<Object> initProviderInvoker(String key, String methodName, RpcInvocation invocation) {
        if (invokerMap.containsKey(key)) {
            return invokerMap.get(key);
        }
        Invoker<Object> wrapperInvoker;
        Invoker<Object> invoker = new RpcProviderInvoker<>(getBeanFunc);
        Class clazz = ClassUtils.getClassByName(invocation.getClassName());
        invoker.setInterface(clazz);


        URL url = invocation.getUrl().copy();
        url.setHost(NetUtils.getLocalHost());
        url.setPort(0);

        invoker.setUrl(url);


        Provider typeProvider = invoker.getInterface().getAnnotation(Provider.class);
        setTypeParameters(typeProvider, url.getParameters());

        Method method = this.getMethod(invocation.getClassName(), methodName, invocation.getParameterTypeNames());
        invoker.setMethod(method);


        MethodAccess methodAccess = MethodAccess.get(ClassUtils.getClassByName(invocation.getClassName()));
        invoker.setMethodAccess(methodAccess);

        Provider methodProvider = method.getAnnotation(Provider.class);

        //方法级别的会覆盖type级别的
        if (null != methodProvider) {
            setMethodParameters(methodProvider, url.getParameters());
        }

        wrapperInvoker = FilterWrapper.ins().buildInvokerChain(invoker, "", Constants.PROVIDER);

        invokerMap.putIfAbsent(key, wrapperInvoker);
        return wrapperInvoker;
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
    public boolean rejectRequest() {
        return false;
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
