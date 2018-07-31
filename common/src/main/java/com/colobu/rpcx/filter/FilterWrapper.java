package com.colobu.rpcx.filter;

import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.RpcException;
import com.colobu.rpcx.rpc.URL;
import com.colobu.rpcx.rpc.impl.RpcFilterFinder;
import com.colobu.rpcx.rpc.impl.RpcInvocation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FilterWrapper {

    private List<Filter> filters;


    private FilterWrapper() {
        RpcFilterFinder finder = new RpcFilterFinder("com.colobu");//TODO $---
        Set<Class<?>> set = finder.find();
        this.filters = set.stream().map(it -> {
            try {
                return (Filter) it.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
    }

    private static class LazyHolder {
        public static FilterWrapper ins = new FilterWrapper();
    }

    public static FilterWrapper ins() {
        return LazyHolder.ins;
    }


    public <T> Invoker<T> buildInvokerChain(final Invoker<T> invoker, String key, String group) {
        Invoker<T> last = invoker;
        if (filters.size() > 0) {
            for (int i = filters.size() - 1; i >= 0; i--) {
                final Filter filter = filters.get(i);
                final Invoker<T> next = last;
                last = new Invoker<T>() {

                    public Class<T> getInterface() {
                        return invoker.getInterface();
                    }

                    public URL getUrl() {
                        return invoker.getUrl();
                    }

                    public boolean isAvailable() {
                        return invoker.isAvailable();
                    }

                    public Result invoke(RpcInvocation invocation) throws RpcException {
                        return filter.invoke(next, invocation);
                    }

                    public void destroy() {
                        invoker.destroy();
                    }

                    @Override
                    public String toString() {
                        return invoker.toString();
                    }
                };
            }
        }
        return last;
    }


}
