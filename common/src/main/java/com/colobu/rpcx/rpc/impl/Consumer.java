package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.netty.IClient;
import com.colobu.rpcx.rpc.CglibProxy;
import com.colobu.rpcx.rpc.ReflectUtils;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.annotation.Provider;

import java.util.stream.Stream;

public class Consumer {




    public <T> T refer(Class<T> clazz) {
        return new CglibProxy().getProxy(clazz, (method, args) -> {
            Provider provider = clazz.getAnnotation(Provider.class);
            RpcInvocation invocation = new RpcInvocation();
            invocation.setClassName(provider.impl());
            invocation.setArguments(args);
            invocation.setMethodName(method.getName());

            Class<?>[] types = method.getParameterTypes();
            invocation.parameterTypeNames = Stream.of(types).map(it -> ReflectUtils.getDesc(it)).toArray(String[]::new);

            invocation.setParameterTypes(types);
            RpcInvoker invoker = new RpcInvoker();
            Result result = invoker.invoke(invocation);
            return result.getValue();
        });
    }

}
