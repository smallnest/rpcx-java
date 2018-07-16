package com.colobu.rpcx.aop;

import com.colobu.rpcx.rpc.annotation.Provider;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created by zhangzhiyong on 29/05/2018.
 */
//@Aspect
@Configuration
public class AopAspect {

    private static final Logger logger = LoggerFactory.getLogger(AopAspect.class);


//    @Around(value = "execution(* com.colobu.rpcx.service.*.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) {
        Object[] o = joinPoint.getArgs();
        logger.info("params:{}", Arrays.asList(o));
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            logger.info("controller result:{}", result);
            long timeTaken = System.currentTimeMillis() - startTime;
            logger.info(" controller {} use time: {}", joinPoint, timeTaken);
            return result;
        } catch (Throwable ex) {
            return null;
        }
    }

//    @Around(value = "@annotation(provider)")
    public Object aroundProvider(ProceedingJoinPoint joinPoint, Provider provider) {
        String taskUuid = UUID.randomUUID().toString();
        Object[] o = joinPoint.getArgs();
        logger.info("task_aop begin name:{} version:{} id:{} params:{}", provider.name(), provider.version());
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long useTime = System.currentTimeMillis() - startTime;
            logger.info("task_aop finish name:{} id:{} result:{} useTime:{}", provider.name(), taskUuid);
            return result;
        } catch (Throwable throwable) {
            logger.warn("task_aop finish name:{} id:{} error:{}", provider.name(), taskUuid, throwable.getMessage());
            return null;
        }
    }
}
