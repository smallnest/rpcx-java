package com.colobu.rpcx.rpc;

/**
 * Created by goodjava@qq.com.
 */
public interface Node {

    URL getUrl();

    void setUrl(URL url);

    boolean isAvailable();

    void destroy();

}
