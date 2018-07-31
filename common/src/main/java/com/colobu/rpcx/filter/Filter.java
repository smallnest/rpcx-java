package com.colobu.rpcx.filter;


import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.RpcException;
import com.colobu.rpcx.rpc.impl.RpcInvocation;

public interface Filter {

    Result invoke(Invoker<?> invoker, RpcInvocation invocation) throws RpcException;

}