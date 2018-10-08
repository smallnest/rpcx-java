package com.colobu.rpcx.config;


import com.colobu.rpcx.fail.FailType;
import com.colobu.rpcx.selector.SelectMode;
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
        return rpcxConsumer.wrap(ITestService.class, new ConsumerConfigBuilder()
//                .setGroup("test")
                .setToken("zzy123")
                .setSendType(Constants.ASYNC_KEY)
                .setTimeout(600000)
                .setFailType(FailType.FailTry)
                .setSelectMode(SelectMode.WeightedRoundRobin)
                .setRetryNum(3));
    }
}
