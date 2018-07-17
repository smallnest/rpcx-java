package com.colobu.rpcx.service;

import com.colobu.rpcx.rpc.annotation.Consumer;

@Consumer(impl = "com.colobu.rpcx.service.TestService")
public interface ITestService {

    String test();

}
