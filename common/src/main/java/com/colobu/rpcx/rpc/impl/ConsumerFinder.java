package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.rpc.annotation.Consumer;
import com.colobu.rpcx.rpc.annotation.Provider;
import org.reflections.Reflections;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by goodjava@qq.com.
 */
public class ConsumerFinder {

    public Set<String> find() {
        Reflections reflections = new Reflections("com.colobu");
        Set<Class<?>> classesList = reflections.getTypesAnnotatedWith(Consumer.class,true);//不包括实现类
        return  classesList.stream().map(it->{
            Consumer consumer = it.getAnnotation(Consumer.class);
            return consumer.impl();
        }).collect(Collectors.toSet());
    }

}
