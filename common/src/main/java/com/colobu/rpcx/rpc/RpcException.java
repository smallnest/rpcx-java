package com.colobu.rpcx.rpc;

public class RpcException extends RuntimeException {

    public RpcException(String message) {
        super(message);
    }

    public RpcException(Exception ex) {
        super(ex);
    }
}
