package com.colobu.rpcx.spring;

import com.colobu.rpcx.rpc.annotation.Consumer;
import com.colobu.rpcx.rpc.annotation.Provider;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Configuration
@Aspect
@ConditionalOnClass(ExampleService.class)
public class ExampleAutoConfigure {

    private static final Logger logger = LoggerFactory.getLogger(ExampleAutoConfigure.class);


    @Autowired
    private ApplicationContext context;


    @PostConstruct
    private void init() {
        logger.info("------------------->{}", context);
//        AutowireCapableBeanFactory bf = this.context.getAutowireCapableBeanFactory();
//        String str = "abc";
//        bf.initializeBean(str, "abc");

        GenericApplicationContext c = (GenericApplicationContext) context;
        c.getBeanFactory().registerSingleton("str", new String("--------->"));

//        Reflections reflections = new Reflections("com.colobu");
//        Set<Class<?>> classesList = reflections.getTypesAnnotatedWith(Consumer.class,true);//不包括实现类
//
//        classesList.stream().forEach(it->{
//            System.out.println(it);
//        });

//        c.getBeanFactory().registerSingleton("testService",new ITes);

//        c.refresh();


    }

    @Bean
    @ConditionalOnMissingBean
    ExampleService exampleService() {

        System.out.println("XXXXXXXXXXXXXXXX");

//        GenericApplicationContext c = (GenericApplicationContext) context;
//        c.getBeanFactory().registerSingleton("str", new String("--------->"));


        return new ExampleService("<", ">" + context);
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
