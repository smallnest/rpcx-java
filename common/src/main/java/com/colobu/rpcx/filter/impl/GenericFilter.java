package com.colobu.rpcx.filter.impl;


import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.Filter;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.ReflectUtils;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.RpcException;
import com.colobu.rpcx.rpc.annotation.RpcFilter;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import com.google.gson.Gson;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * @author goodjava@qq.com
 */
@RpcFilter(group = {Constants.PROVIDER}, order = -2001)
public class GenericFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, RpcInvocation inv) throws RpcException {
        if (inv.getMethodName().equals(Constants.$INVOKE)) {
            String[] types = inv.getParameterTypeNames();
            final String[] args = (String[]) inv.getArguments();

            Gson gson = new Gson();
            int len = types.length;

            Class[] classTypes = new Class[types.length];

            Object[] params = IntStream.range(0, len).mapToObj(i -> {
                String type = types[i];
                Class<?> clazz = ReflectUtils.forName(type);
                classTypes[i] = clazz;
                int j = i + 1;
                String arg = args[j];
                Object obj = gson.fromJson(arg, clazz);
                return obj;
            }).toArray(Object[]::new);

            //从新组装Invocation
            inv.setMethodName(args[0]);
            inv.setParameterTypes(classTypes);
            inv.setArguments(params);

            Result res = invoker.invoke(inv);

            if (!res.hasException()) {
                Object value = res.getValue();
                res.setValue(gson.toJson(value));
                return res;
            }

        }

        return invoker.invoke(inv);
    }

}