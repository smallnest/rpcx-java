package com.colobu.rpcx.cache;


import com.colobu.rpcx.cache.impl.LruCache;
import com.colobu.rpcx.rpc.URL;

/**
 * @author goodjava@qq.com
 */
public class LruCacheFactory extends AbstractCacheFactory {

    @Override
    protected Cache createCache(URL url) {
        return new LruCache();
    }

}
