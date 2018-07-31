package com.colobu.rpcx.cache;


import com.colobu.rpcx.cache.impl.LruCache;
import com.colobu.rpcx.rpc.URL;

/**
 * LruCacheFactory
 * 
 * @author william.liangf
 */
public class LruCacheFactory extends AbstractCacheFactory {

    protected Cache createCache(URL url) {
        return new LruCache();
    }

}
