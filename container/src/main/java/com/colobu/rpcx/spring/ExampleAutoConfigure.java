package com.colobu.rpcx.spring;

import com.colobu.rpcx.rpc.annotation.Provider;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;


@Configuration
@Aspect
@ConditionalOnClass(ExampleService.class)
public class ExampleAutoConfigure {

    private static final Logger logger = LoggerFactory.getLogger(ExampleAutoConfigure.class);

    @Bean
    @ConditionalOnMissingBean
    ExampleService exampleService() {
        return new ExampleService("<", ">");
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
