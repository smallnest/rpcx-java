package com.colobu.rpcx.service;

import com.colobu.rpcx.rpc.annotation.Consumer;

@Consumer(impl = "com.colobu.rpcx.service.TestService")
public interface ITestService {

    String hi(String str);

    String $echo(String str);

    byte[] golangHi(byte[] data);

}
