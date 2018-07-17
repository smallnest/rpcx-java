package com.colobu.rpcx.service;

import com.colobu.rpcx.rpc.annotation.Provider;
import org.springframework.stereotype.Service;


@Service
@Provider
public class TestService implements ITestService {


    public String hi(String str) {
        return "hi " + str;
    }

}
