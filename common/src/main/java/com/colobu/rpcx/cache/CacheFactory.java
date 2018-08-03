package com.colobu.rpcx.cache;


import com.colobu.rpcx.rpc.URL;

/**
 * @author goodjava@qq.com
 */
public interface CacheFactory {

    Cache getCache(URL url);

}
