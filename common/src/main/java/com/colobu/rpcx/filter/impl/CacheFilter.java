package com.colobu.rpcx.filter.impl;


import com.colobu.rpcx.cache.Cache;
import com.colobu.rpcx.cache.LruCacheFactory;
import com.colobu.rpcx.cache.impl.LruCache;
import com.colobu.rpcx.common.StringUtils;
import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.Filter;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.RpcException;
import com.colobu.rpcx.rpc.annotation.RpcFilter;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import com.colobu.rpcx.rpc.impl.RpcResult;

/**
 * CacheFilter
 */
@RpcFilter
public class CacheFilter implements Filter {

    private static LruCacheFactory factory = new LruCacheFactory();

    public Result invoke(Invoker<?> invoker, RpcInvocation invocation) throws RpcException {
        if (invoker.getUrl().getParameter(Constants.CACHE_KEY).equals("true")) {
            Cache cache = factory.getCache(invoker.getUrl());
            if (cache != null) {
                String key = StringUtils.toArgumentString(invocation.getArguments());
                if (cache != null && key != null) {
                    Object value = cache.get(key);
                    if (value != null) {
                        return new RpcResult(value);
                    }
                    Result result = invoker.invoke(invocation);
                    if (! result.hasException()) {
                        cache.put(key, result.getValue());
                    }
                    return result;
                }
            }
        }
        return invoker.invoke(invocation);
    }

}
