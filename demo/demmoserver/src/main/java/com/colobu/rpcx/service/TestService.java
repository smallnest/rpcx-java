package com.colobu.rpcx.service;

import com.colobu.rpcx.rpc.annotation.Provider;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
@Provider(name = "TestService", version = "0.0.2", token = "zzy123")
public class TestService implements ITestService {


    public String hi(String str) {
//        try {
//            TimeUnit.SECONDS.sleep(2);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return "hi " + str;
    }

}
