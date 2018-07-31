package com.colobu.rpcx.rpc;

public interface Node {

    URL getUrl();

    boolean isAvailable();

    void destroy();

}
