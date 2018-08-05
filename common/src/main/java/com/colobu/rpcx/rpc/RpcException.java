package com.colobu.rpcx.rpc;


/**
 * @author goodjava@qq.com
 */
public class RpcException extends RuntimeException {

    private int code;

    private String message;

    public RpcException(String message, int code) {
        super(message);
        this.code = code;
    }

    public RpcException(String message, Throwable cause, int code) {
        super(cause);
        this.message = message;
        this.code = code;
    }

    public RpcException(Throwable cause) {
        super(cause);
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

    @Override
    public String getMessage() {
        return this.message;
    }
}
