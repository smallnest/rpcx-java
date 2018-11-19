package com.colobu.rpcx.config;


import com.colobu.rpcx.rpc.impl.ConsumerConfig.ConsumerConfigBuilder;
import com.colobu.rpcx.service.ITest2Service;
import com.colobu.rpcx.spring.RpcxConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author goodjava@qq.com
 */
@Configuration
public class ConsumerConfig {

    @Autowired
    private RpcxConsumer rpcxConsumer;


    @Bean
    public ITest2Service test2Service() {
        return rpcxConsumer.wrap(ITest2Service.class, new ConsumerConfigBuilder().setRetryNum(3));
    }
}
