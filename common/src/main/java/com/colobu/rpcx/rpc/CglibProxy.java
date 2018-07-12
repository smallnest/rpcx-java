package com.colobu.rpcx.rpc;

import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

public class CglibProxy {


    public <T> T getProxy(Class<?> clazz, final BiFunction<Method, Object[], Object> function) {
        Enhancer e = new Enhancer();
        e.setSuperclass(clazz);
        e.setCallback((net.sf.cglib.proxy.InvocationHandler) (proxy, method, args) -> {
            return function.apply(method,args);
        });
        return (T) e.create();
    }


}
