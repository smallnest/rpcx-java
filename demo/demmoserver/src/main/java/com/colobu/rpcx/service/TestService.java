package com.colobu.rpcx.service;

import com.colobu.rpcx.rpc.annotation.Provider;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
@Provider(name = "TestService", version = "0.0.2", token = "zzy123", timeout = 1000)
public class TestService implements ITestService {


    public String hi(String str) {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hi " + str;
    }

}
