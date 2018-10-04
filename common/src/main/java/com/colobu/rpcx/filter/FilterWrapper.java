package com.colobu.rpcx.filter;

import com.colobu.rpcx.common.Config;
import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.RpcException;
import com.colobu.rpcx.rpc.URL;
import com.colobu.rpcx.rpc.impl.RpcFilterFinder;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author goodjava@qq.com
 */
public class FilterWrapper {

    private static final Logger logger = LoggerFactory.getLogger(FilterWrapper.class);

    private List<Filter> providerFilters;
    private List<Filter> consumerFilters;


    private FilterWrapper() {
        this.providerFilters = this.findFilters("com.colobu", Constants.PROVIDER);
        this.consumerFilters = this.findFilters("com.colobu", Constants.CONSUMER);

        String p = Config.ins().get("rpcx_filter_package");

        this.providerFilters.addAll(this.findFilters(p, Constants.PROVIDER));
        this.providerFilters.addAll(this.findFilters(p, Constants.CONSUMER));
    }


    private List<Filter> findFilters(String packageName, String group) {
        RpcFilterFinder finder = new RpcFilterFinder(packageName, group);
        List<Class> list = finder.find();
        logger.info("################" + group + " " + list);
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

    /**
     *
     * @param invoker
     * @param key
     * @param group
     * @param excludeFilters 不需要构建的filter
     * @param <T>
     * @return
     */
    public <T> Invoker<T> buildInvokerChain(final Invoker<T> invoker, String key, String group, Set<String> excludeFilters) {
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

                //如果没有启用此filter,则不加入此filter到chain
                if (excludeFilters.contains(filter.name())) {
                    continue;
                }

                final Invoker<T> next = last;
                last = new Invoker<T>() {

                    @Override
                    public Class<T> getInterface() {
                        return invoker.getInterface();
                    }

                    @Override
                    public URL getUrl() {
                        return invoker.getUrl();
                    }

                    @Override
                    public void setUrl(URL url) {
                        invoker.setUrl(url);
                    }

                    @Override
                    public boolean isAvailable() {
                        return invoker.isAvailable();
                    }

                    @Override
                    public Result invoke(RpcInvocation invocation) throws RpcException {
                        return filter.invoke(next, invocation);
                    }

                    @Override
                    public void setMethod(Method method) {
                        invoker.setMethod(method);
                    }

                    @Override
                    public Method getMethod() {
                        return invoker.getMethod();
                    }

                    @Override
                    public void setInterface(Class clazz) {
                        invoker.setInterface(clazz);
                    }

                    @Override
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
