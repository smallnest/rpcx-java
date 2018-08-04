package com.colobu.rpcx.spring;

import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.FilterWrapper;
import com.colobu.rpcx.netty.IClient;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.ReflectUtils;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.impl.ConsumerConfig;
import com.colobu.rpcx.rpc.impl.RpcConsumerInvoker;
import com.colobu.rpcx.rpc.impl.RpcInvocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author goodjava@qq.com
 */
public class RpcxConsumer {

    private final IClient client;

    public RpcxConsumer(IClient client) {
        this.client = client;
    }

    /**
     * 全部使用默认配置
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T wrap(Class clazz) {
        return (T) new ConsumerConfig(client).refer(clazz);
    }

    /**
     * 可以传入配置 如超时时间等
     *
     * @param clazz
     * @param builder
     * @param <T>
     * @return
     */
    public <T> T wrap(Class clazz, ConsumerConfig.ConsumerConfigBuilder builder) {
        return (T) builder.setClient(client).build().refer(clazz);
    }

    /**
     * 泛化调用
     */
    public String invoke(String className, String methodName, String[] types, String[] params) {
        RpcInvocation invocation = new RpcInvocation();
        invocation.setMethodName(Constants.$INVOKE);
        invocation.setClassName(className);
        invocation.setParameterTypeNames(types);
        invocation.setTimeOut(2000);
        invocation.setRetryNum(1);
        String[] params1 = new String[params.length + 1];
        params1[0] = methodName;
        System.arraycopy(params, 0, params1, 1, params.length);
        invocation.setArguments(params1);
        RpcConsumerInvoker invoker = new RpcConsumerInvoker(client, invocation);
        Invoker<Object> wrapperInvoker = FilterWrapper.ins().buildInvokerChain(invoker, "", Constants.CONSUMER);
        Result result = wrapperInvoker.invoke(invocation);
        if (result.hasException()) {
            throw new RuntimeException(result.getException());
        }
        return result.getValue().toString();
    }

    /**
     * echo 调用
     *
     * @return
     */
    public String echo(String className, String str) {
        RpcInvocation invocation = new RpcInvocation();
        invocation.setClassName(className);
        invocation.setMethodName(Constants.$ECHO);
        invocation.setParameterTypeNames(new String[]{ReflectUtils.getName(String.class)});
        invocation.setTimeOut(1000);
        invocation.setRetryNum(1);
        invocation.setArguments(new String[]{str});
        RpcConsumerInvoker invoker = new RpcConsumerInvoker(client, invocation);
        Invoker<Object> wrapperInvoker = FilterWrapper.ins().buildInvokerChain(invoker, "", Constants.CONSUMER);
        Result result = wrapperInvoker.invoke(invocation);
        if (result.hasException()) {
            throw new RuntimeException(result.getException());
        }
        return result.getValue().toString();
    }


    /**
     * 热部署
     *
     * @param className
     * @param classPath
     * @param token
     * @return
     * @throws IOException
     */
    public String deploy(String className, String classPath, String token) throws IOException {
        RpcInvocation invocation = new RpcInvocation();
        invocation.setMethodName(Constants.$HOT_DEPLOY);
        invocation.setClassName(className);

        String[] paramNames = new String[3];
        paramNames[0] = ReflectUtils.getName(String.class);
        paramNames[1] = ReflectUtils.getName(String.class);
        paramNames[2] = ReflectUtils.getName(byte[].class);

        invocation.setParameterTypeNames(paramNames);
        invocation.setTimeOut(2000);
        invocation.setRetryNum(1);
        byte[] classData = Files.readAllBytes(Paths.get(classPath));
        Object[] params = new Object[]{className, token, classData};
        invocation.setArguments(params);
        RpcConsumerInvoker invoker = new RpcConsumerInvoker(client, invocation);
        Result result = invoker.invoke(invocation);
        if (result.hasException()) {
            throw new RuntimeException(result.getException());
        }
        return result.getValue().toString();
    }
}
