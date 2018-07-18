package com.colobu.rpcx.config;


import com.colobu.rpcx.service.IArith;
import com.colobu.rpcx.service.ITestService;
import com.colobu.rpcx.spring.RpcxConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.colobu.rpcx.rpc.impl.ConsumerConfig.ConsumerConfigBuilder;

@Configuration
public class ConsumerConfig {

    @Autowired
    private RpcxConsumer rpcxConsumer;

    @Bean
    public IArith arith() {
        return rpcxConsumer.wrap(IArith.class);
    }

    @Bean
    public ITestService testService() {
        ConsumerConfigBuilder builder = new ConsumerConfigBuilder();
        builder.setTimeout(1000);//指定超时
        return rpcxConsumer.wrap(ITestService.class, builder);
    }
}
