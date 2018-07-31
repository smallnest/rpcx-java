package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.rpc.annotation.RpcFilter;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RpcFilterFinder {

    private final String filterPackage;

    public RpcFilterFinder(String providerPackage) {
        this.filterPackage = providerPackage;
    }


    public List<Class> find() {
        Reflections reflections = new Reflections(filterPackage);
        Set<Class<?>> classesList = reflections.getTypesAnnotatedWith(RpcFilter.class);
        List<Class> list = new ArrayList<>(classesList);
        return list.stream().sorted((a, b) -> {
            RpcFilter a1 = (RpcFilter) a.getAnnotation(RpcFilter.class);
            RpcFilter b1 = (RpcFilter) b.getAnnotation(RpcFilter.class);
            return a1.order() - b1.order();
        }).collect(Collectors.toList());
    }

}
