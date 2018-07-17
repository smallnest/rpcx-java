package com.colobu.rpcx.config;


import com.colobu.rpcx.service.IArith;
import com.colobu.rpcx.spring.RpcxConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsumerConfig {

    @Autowired
    private RpcxConsumer rpcxConsumer;

    @Bean
    public IArith arith() {
        return rpcxConsumer.wrap(IArith.class);
    }
}
