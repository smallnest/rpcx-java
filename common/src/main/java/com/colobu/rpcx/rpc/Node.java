package com.colobu.rpcx.rpc;

/**
 * Created by goodjava@qq.com.
 */
public interface Node {

    URL getUrl();

    boolean isAvailable();

    void destroy();

}
