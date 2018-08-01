package com.colobu.rpcx.filter;

import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.RpcException;
import com.colobu.rpcx.rpc.URL;
import com.colobu.rpcx.rpc.impl.RpcFilterFinder;
import com.colobu.rpcx.rpc.impl.RpcInvocation;

import java.util.List;
import java.util.stream.Collectors;

public class FilterWrapper {

    private List<Filter> providerFilters;
    private List<Filter> consumerFilters;


    private FilterWrapper() {
        this.providerFilters = this.findFilters(Constants.PROVIDER);
        this.consumerFilters = this.findFilters(Constants.CONSUMER);
    }


    private List<Filter> findFilters(String group) {
        RpcFilterFinder finder = new RpcFilterFinder("com.colobu", group);
        List<Class> list = finder.find();
        System.out.println("################" + group + " " + list);
        return list.stream().map(it -> {
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
        List<Filter> list = null;
        if (group.equals(Constants.PROVIDER)) {
            list = providerFilters;
        } else {
            list = consumerFilters;
        }
        if (list.size() > 0) {
            for (int i = list.size() - 1; i >= 0; i--) {
                final Filter filter = list.get(i);
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
