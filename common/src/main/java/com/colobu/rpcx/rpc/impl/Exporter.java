package com.colobu.rpcx.rpc.impl;

import java.util.Set;
import java.util.stream.Collectors;

public class Exporter {

    public Set<String> export() {
        Set<Class<?>> set = new ProviderFinder().find();
        return set.stream().map(it->it.getName()).collect(Collectors.toSet());
    }
}
