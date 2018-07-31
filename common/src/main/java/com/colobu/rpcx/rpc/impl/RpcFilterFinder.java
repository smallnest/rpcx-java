package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.rpc.annotation.RpcFilter;
import org.reflections.Reflections;

import java.util.Set;

public class RpcFilterFinder {

    private final String filterPackage;

    public RpcFilterFinder(String providerPackage) {
        this.filterPackage = providerPackage;
    }


    public Set<Class<?>> find() {
        Reflections reflections = new Reflections(filterPackage);
        Set<Class<?>> classesList = reflections.getTypesAnnotatedWith(RpcFilter.class);
        return classesList;
    }

}
