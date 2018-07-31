package com.colobu.rpcx.cache;


import com.colobu.rpcx.rpc.URL;

/**
 * CacheFactory
 */
public interface CacheFactory {

    Cache getCache(URL url);

}
