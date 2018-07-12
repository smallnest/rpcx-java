package com.colobu.rpcx.rpc;

public interface Invoker<T> {

    Class<T> getInterface();

    Result invoke(Invocation invocation) throws RpcException;
}
