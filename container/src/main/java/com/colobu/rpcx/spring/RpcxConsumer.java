package com.colobu.rpcx.spring;

import com.colobu.rpcx.netty.IClient;
import com.colobu.rpcx.rpc.impl.ConsumerConfig;

public class RpcxConsumer {

    private final IClient client;

    public RpcxConsumer(IClient client) {
        this.client = client;
    }

    /**
     * 全部使用默认配置
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T wrap(Class clazz) {
        return (T) new ConsumerConfig(client).refer(clazz);
    }

    /**
     * 可以传入配置 如超时时间等
     * @param clazz
     * @param builder
     * @param <T>
     * @return
     */
    public <T> T wrap(Class clazz, ConsumerConfig.ConsumerConfigBuilder builder) {
        return (T) builder.setClient(client).build().refer(clazz);
    }
}
