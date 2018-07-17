package com.colobu.rpcx.spring;

import com.colobu.rpcx.client.IServiceDiscovery;
import com.colobu.rpcx.client.NettyClient;
import com.colobu.rpcx.client.ZkServiceDiscovery;
import com.colobu.rpcx.netty.IClient;
import com.colobu.rpcx.rpc.annotation.Consumer;
import com.colobu.rpcx.rpc.annotation.Provider;
import com.colobu.rpcx.rpc.impl.ConsumerConfig;
import com.colobu.rpcx.server.IServiceRegister;
import com.colobu.rpcx.server.NettyServer;
import com.colobu.rpcx.server.ZkServiceRegister;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.UUID;


@Configuration
@Aspect
@ConditionalOnClass(RpcxService.class)
public class RpcxAutoConfigure {

    private static final Logger logger = LoggerFactory.getLogger(RpcxAutoConfigure.class);


    @Autowired
    private ApplicationContext context;


    @PostConstruct
    private void init() {
        IServiceDiscovery serviceDiscovery = new ZkServiceDiscovery("/youpin/services/");
        IClient client = new NettyClient(serviceDiscovery);

        GenericApplicationContext c = (GenericApplicationContext) context;
        Reflections reflections = new Reflections("com.colobu");
        Set<Class<?>> classesList = reflections.getTypesAnnotatedWith(Consumer.class, true);//不包括实现类

        //把consumer都注入进来
        classesList.stream().forEach(it -> {
            System.out.println(it.getSimpleName());
            Object o = new ConsumerConfig(client).refer(it);
            c.getBeanFactory().registerSingleton(it.getSimpleName(), o);
        });


        //启动provider
        Set<Class<?>> providerSet = reflections.getTypesAnnotatedWith(Provider.class, true);
        providerSet.stream().forEach(it -> {
            logger.info("provider:{}", it);
        });

        NettyServer server = new NettyServer();
        server.setUseSpring(true);
        server.setGetBean((clazz) -> {
            return context.getBean(clazz);
        });
        server.start();
        IServiceRegister reg = new ZkServiceRegister("/youpin/services/", server.getAddr() + ":" + server.getPort(), "com.colobu");
        reg.register();
        reg.start();
    }

    @Bean
    @ConditionalOnMissingBean
    RpcxService exampleService() {
        return new RpcxService("<", ">" + context);
    }


    //    @Around(value = "@annotation(provider)")
    @Around("execution(public * *(..)) && within(@com.colobu.rpcx.rpc.annotation.Provider *)")
    public Object aroundProvider(ProceedingJoinPoint joinPoint) {
        Provider provider = joinPoint.getTarget().getClass().getAnnotation(Provider.class);
        String taskUuid = UUID.randomUUID().toString();
        Object[] o = joinPoint.getArgs();
        logger.info("task_aop begin name:{} version:{} id:{} params:{}", provider.name(), provider.version());
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long useTime = System.currentTimeMillis() - startTime;
//            logger.info("task_aop finish name:{} id:{} result:{} useTime:{}", provider.name(), taskUuid);
            return result;
        } catch (Throwable throwable) {
//            logger.warn("task_aop finish name:{} id:{} error:{}", provider.name(), taskUuid, throwable.getMessage());
            return null;
        }
    }

}
