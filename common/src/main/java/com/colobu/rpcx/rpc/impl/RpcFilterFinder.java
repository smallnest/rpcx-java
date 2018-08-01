package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.rpc.annotation.RpcFilter;
import org.reflections.Reflections;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RpcFilterFinder {

    private final String filterPackage;
    private final String group;

    public RpcFilterFinder(String providerPackage, String group) {
        this.filterPackage = providerPackage;
        this.group = group;
    }


    public List<Class> find() {
        Reflections reflections = new Reflections(filterPackage);
        Set<Class<?>> classesList = reflections.getTypesAnnotatedWith(RpcFilter.class);
        List<Class> list = classesList.stream().filter(it -> {
            RpcFilter an = it.getAnnotation(RpcFilter.class);
            return Stream.of(an.group()).anyMatch(it2 -> it2.equals(group));//过滤出provider
        }).collect(Collectors.toList());
        return list.stream().sorted((a, b) -> {
            RpcFilter a1 = (RpcFilter) a.getAnnotation(RpcFilter.class);
            RpcFilter b1 = (RpcFilter) b.getAnnotation(RpcFilter.class);
            return a1.order() - b1.order();
        }).collect(Collectors.toList());
    }

}
