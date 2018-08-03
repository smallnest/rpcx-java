package com.colobu.rpcx.exception;

/**
 * @author goodjava@qq.com
 */
public class RemotingTooMuchRequestException extends RemotingException {
    private static final long serialVersionUID = 4326919581254519654L;


    public RemotingTooMuchRequestException(String message) {
        super(message);
    }
}
