package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.rpc.annotation.Provider;
import org.reflections.Reflections;

import java.util.Set;


/**
 * Created by goodjava@qq.com.
 */
public class ProviderFinder {

    private final String providerPackage;

    public ProviderFinder(String providerPackage) {
        this.providerPackage = providerPackage;
    }


    public Set<Class<?>> find() {
        Reflections reflections = new Reflections(providerPackage);
        Set<Class<?>> classesList = reflections.getTypesAnnotatedWith(Provider.class);
        return classesList;
    }

}
