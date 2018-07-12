package com.colobu.rpcx.rpc;

public class RpcException extends RuntimeException {

    public RpcException(Exception ex) {
        super(ex);
    }
}
