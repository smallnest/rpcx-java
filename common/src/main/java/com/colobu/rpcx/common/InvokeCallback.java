package com.colobu.rpcx.common;


import com.colobu.rpcx.netty.ResponseFuture;

public interface InvokeCallback {
    void operationComplete(final ResponseFuture responseFuture);
}
