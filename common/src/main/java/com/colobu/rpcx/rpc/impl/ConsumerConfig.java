package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.fail.FailType;
import com.colobu.rpcx.filter.FilterWrapper;
import com.colobu.rpcx.netty.IClient;
import com.colobu.rpcx.protocol.LanguageCode;
import com.colobu.rpcx.rpc.*;
import com.colobu.rpcx.rpc.annotation.Consumer;
import com.colobu.rpcx.selector.SelectMode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


/**
 * @author goodjava@qq.com
 */
public class ConsumerConfig {

    private IClient client;

    private long timeOut = TimeUnit.SECONDS.toMillis(2);

    private int retryNum = 1;

    private String token = "";

    private String sendType = Constants.SYNC_KEY;

    private FailType failType = FailType.FailFast;

    private SelectMode selectMode = SelectMode.RandomSelect;

    private String group = "";

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

        public ConsumerConfigBuilder setSendType(String sendType) {
            this.config.sendType = sendType;
            return this;
        }

        public ConsumerConfigBuilder setFailType(FailType failType) {
            this.config.failType = failType;
            return this;
        }

        public ConsumerConfigBuilder setSelectMode(SelectMode selectMode) {
            this.config.selectMode = selectMode;
            return this;
        }

        public ConsumerConfigBuilder setGroup(String group) {
            this.config.group = group;
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
            invocation.setSendType(sendType);
            invocation.setFailType(failType);
            invocation.setSelectMode(selectMode);
            invocation.setLanguageCode(LanguageCode.JAVA);
            invocation.setGroup(group);

            Map<String, String> attachments = new HashMap<>(1);
            attachments.put(Constants.TOKEN_KEY, token);
            invocation.setAttachments(attachments);

            Class<?>[] types = method.getParameterTypes();
            invocation.parameterTypeNames = Stream.of(types).map(it -> ReflectUtils.getName(it)).toArray(String[]::new);
            invocation.setParameterTypes(types);
            invocation.setResultType(method.getReturnType());

            Invoker invoker = new RpcConsumerInvoker(client);
            Invoker<Object> wrapperInvoker = FilterWrapper.ins().buildInvokerChain(invoker, "", Constants.CONSUMER);

            Result result = wrapperInvoker.invoke(invocation);

            if (result.hasException()) {
                if (result.getException() instanceof RpcException) {
                    throw (RpcException) result.getException();
                } else {
                    throw new RpcException(result.getException());
                }
            }

            return result.getValue();
        });
    }


}
