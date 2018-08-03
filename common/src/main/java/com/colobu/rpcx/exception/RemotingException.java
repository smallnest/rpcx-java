package com.colobu.rpcx.exception;


/**
 * Created by goodjava@qq.com.
 */
public class RemotingException extends Exception {
    private static final long serialVersionUID = -5690687334570505110L;


    public RemotingException(String message) {
        super(message);
    }


    public RemotingException(String message, Throwable cause) {
        super(message, cause);
    }

}
