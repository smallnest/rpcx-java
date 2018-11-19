package com.colobu.rpcx.service;


import com.colobu.rpcx.rpc.annotation.Consumer;

@Consumer(impl = "com.colobu.rpcx.service.Test2Service")
public interface ITest2Service {

    int num();

}
