package com.colobu.rpcx.service;

import com.colobu.rpcx.rpc.annotation.Consumer;
import com.colobu.rpcx.rpc.annotation.Provider;
import org.springframework.stereotype.Service;

//@Consumer(impl = "com.colobu.rpcx.service.TestService")
public interface ITestService {

    String test();

}
