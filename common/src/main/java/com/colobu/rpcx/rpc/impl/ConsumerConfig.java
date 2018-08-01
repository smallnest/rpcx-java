package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.FilterWrapper;
import com.colobu.rpcx.netty.IClient;
import com.colobu.rpcx.rpc.CglibProxy;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.ReflectUtils;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.annotation.Consumer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class ConsumerConfig {

    private IClient client;

    private long timeOut = TimeUnit.SECONDS.toMillis(2);

    private int retryNum = 3;

    private String token = "";

    public ConsumerConfig() {
    }

    public ConsumerConfig(IClient client) {
        this.client = client;
    }

    public static class ConsumerConfigBuilder {

        private ConsumerConfig config = new ConsumerConfig();

        public ConsumerConfigBuilder setClient(IClient client) {
            this.config.client = client;
            return this;
        }

        public ConsumerConfigBuilder setTimeout(long timeOut) {
            this.config.timeOut = timeOut;
            return this;
        }

        public ConsumerConfigBuilder setRetryNum(int retryNum) {
            this.config.retryNum = retryNum;
            return this;
        }

        public ConsumerConfigBuilder setToken(String token) {
            this.config.token = token;
            return this;
        }


        public ConsumerConfig build() {
            return this.config;
        }
    }


    public <T> T refer(Class<T> clazz) {
        return new CglibProxy().getProxy(clazz, (method, args) -> {
            Consumer provider = clazz.getAnnotation(Consumer.class);
            RpcInvocation invocation = new RpcInvocation();
            invocation.setClassName(provider.impl());
            invocation.setArguments(args);
            invocation.setMethodName(method.getName());
            invocation.setTimeOut(timeOut);
            invocation.setRetryNum(retryNum);

            Map<String, String> attachments = new HashMap<>();
            attachments.put(Constants.TOKEN_KEY, token);
            invocation.setAttachments(attachments);

            Class<?>[] types = method.getParameterTypes();
            invocation.parameterTypeNames = Stream.of(types).map(it -> ReflectUtils.getDesc(it)).toArray(String[]::new);
            invocation.setParameterTypes(types);
            invocation.setResultType(method.getReturnType());

            RpcConsumerInvoker invoker = new RpcConsumerInvoker(client, invocation);
            Invoker<Object> wrapperInvoker = FilterWrapper.ins().buildInvokerChain(invoker, "", Constants.CONSUMER);

            Result result = wrapperInvoker.invoke(invocation);

            if (result.hasException()) {
                throw new RuntimeException(result.getException());
            }

            return result.getValue();
        });
    }


}
