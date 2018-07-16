package com.colobu.rpcx.service;

import com.colobu.rpcx.rpc.annotation.Provider;
import org.springframework.stereotype.Service;


public class TestService implements ITestService {


    //    @Provider
    public String test() {
        return "test";
    }

}
