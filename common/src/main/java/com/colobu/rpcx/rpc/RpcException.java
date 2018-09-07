package com.colobu.rpcx.rpc;


/**
 * @author goodjava@qq.com
 */
public class RpcException extends RuntimeException {

    private String code;

    public RpcException(String message, String code) {
        super(message);
        this.code = code;
    }

    public RpcException(String message, Throwable cause, String code) {
        super(message, cause);
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


    public String getCode() {
        return code;
    }

}
