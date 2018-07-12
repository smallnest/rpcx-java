package com.colobu.rpcx.rpc;

import com.colobu.rpcx.rpc.impl.RpcInvocation;

public interface Invoker<T> {

    Class<T> getInterface();

    Result invoke(RpcInvocation invocation) throws RpcException;
}
