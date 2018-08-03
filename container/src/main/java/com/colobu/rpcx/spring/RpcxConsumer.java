package com.colobu.rpcx.spring;

import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.FilterWrapper;
import com.colobu.rpcx.netty.IClient;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.impl.ConsumerConfig;
import com.colobu.rpcx.rpc.impl.RpcConsumerInvoker;
import com.colobu.rpcx.rpc.impl.RpcInvocation;

/**
 * Created by goodjava@qq.com.
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
}
