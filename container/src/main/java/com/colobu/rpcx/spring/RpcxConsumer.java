package com.colobu.rpcx.spring;

import com.colobu.rpcx.netty.IClient;
import com.colobu.rpcx.rpc.impl.ConsumerConfig;

public class RpcxConsumer {

    private final IClient client;

    public RpcxConsumer(IClient client) {
        this.client = client;
    }

    public <T> T wrap(Class clazz) {
        return (T) new ConsumerConfig(client).refer(clazz);
    }

    public <T> T wrap(Class clazz, ConsumerConfig.ConsumerConfigBuilder config) {
        return (T) config.setClient(client).build().refer(clazz);
    }
}
