package com.colobu.rpcx.common;


import com.colobu.rpcx.netty.ResponseFuture;

/**
 * @author goodjava@qq.com
 */
public interface InvokeCallback {
    void operationComplete(final ResponseFuture responseFuture);
}
