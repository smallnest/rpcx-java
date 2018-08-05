package com.colobu.rpcx.config;


import com.colobu.rpcx.fail.FailType;
import com.colobu.rpcx.service.IArith;
import com.colobu.rpcx.service.ITestService;
import com.colobu.rpcx.spring.RpcxConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.colobu.rpcx.rpc.impl.ConsumerConfig.ConsumerConfigBuilder;

/**
 * @author goodjava@qq.com
 */
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
        //设置超时时间1000 重试次数3
        return rpcxConsumer.wrap(ITestService.class, new ConsumerConfigBuilder()
                .setToken("zzy123")
                .setSendType(Constants.SYNC_KEY)
                .setTimeout(600000)
                .setFailType(FailType.FailOver.name())
                .setRetryNum(3));
    }
}
