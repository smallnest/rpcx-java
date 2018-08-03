package com.colobu.rpcx.rpc.impl;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by goodjava@qq.com.
 */
public class Exporter {

    public Set<String> export(String providerPackage) {
        Set<Class<?>> set = new ProviderFinder(providerPackage).find();
        return set.stream().map(it->it.getName()).collect(Collectors.toSet());
    }
}
