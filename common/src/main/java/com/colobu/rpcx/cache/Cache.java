package com.colobu.rpcx.cache;

/**
 * Created by goodjava@qq.com.
 */
public interface Cache {

    void put(Object key, Object value);

    Object get(Object key);

}
