package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.rpc.annotation.Provider;
import org.reflections.Reflections;

import java.util.Set;

public class ProviderFinder {

    public Set<Class<?>> find() {
        Reflections reflections = new Reflections("com.colobu");
        Set<Class<?>> classesList = reflections.getTypesAnnotatedWith(Provider.class);
        return classesList;
    }

}
