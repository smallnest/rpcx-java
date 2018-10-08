package com.colobu.rpcx.rpc;

import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

/**
 * Created by goodjava@qq.com.
 */
public class CglibProxy {


    public <T> T getProxy(Class<?> clazz, final BiFunction<Method, Object[], Object> function) {
        Enhancer e = new Enhancer();
        e.setSuperclass(clazz);
        e.setCallback((net.sf.cglib.proxy.InvocationHandler) (proxy, method, args) -> {
            String methodName = method.getName();
            if ("getClass".equals(methodName)) {
                return proxy.getClass();
            }
            if ("hashCode".equals(methodName)) {
                return proxy.hashCode();
            }
            if ("toString".equals(methodName)) {
                return proxy.toString();
            }
            return function.apply(method, args);
        });
        return (T) e.create();
    }


}
