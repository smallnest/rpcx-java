package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.rpc.annotation.Consumer;
import com.colobu.rpcx.rpc.annotation.Provider;
import org.reflections.Reflections;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author goodjava@qq.com
 */
public class ConsumerFinder {

    public Set<String> find(String consumerPackage) {
        Reflections reflections = new Reflections(consumerPackage);
        //不包括实现类
        Set<Class<?>> classesList = reflections.getTypesAnnotatedWith(Consumer.class,true);
        return  classesList.stream().map(it->{
            Consumer consumer = it.getAnnotation(Consumer.class);
            return consumer.impl();
        }).collect(Collectors.toSet());
    }

}
