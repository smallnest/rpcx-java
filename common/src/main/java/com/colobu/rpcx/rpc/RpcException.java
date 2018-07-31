package com.colobu.rpcx.rpc;

public class RpcException extends RuntimeException {


    private int code;
    private String message;

    public RpcException(String message, int code) {
        super(message);
        this.code = code;
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(Exception ex) {
        super(ex);
    }


    public int getCode() {
        return code;
    }
}
