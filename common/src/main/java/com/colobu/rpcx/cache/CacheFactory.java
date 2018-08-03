package com.colobu.rpcx.cache;


import com.colobu.rpcx.rpc.URL;

/**
 * Created by goodjava@qq.com.
 */
public interface CacheFactory {

    Cache getCache(URL url);

}
