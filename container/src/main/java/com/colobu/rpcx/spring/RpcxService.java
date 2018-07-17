package com.colobu.rpcx.spring;

public class RpcxService {

    private String prefix;
    private String suffix;

    public RpcxService(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }


    public String wrap(String word) {
        return prefix + word + suffix;
    }

}
