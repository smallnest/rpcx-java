package com.colobu.rpcx.service;

import com.colobu.rpcx.rpc.annotation.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
@Provider(name = "TestService", version = "0.0.2", token = "zzy123", timeout = 1000, cache = true)
public class TestService implements ITestService {

    private static final Logger logger = LoggerFactory.getLogger(TestService.class);

    public String hi(String str) {
        logger.info("-------------->call hi:{}", str);
//        try {
//            TimeUnit.SECONDS.sleep(5);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return "hi " + str;
    }

    //不会调过来
    public String $echo(String str) {
        return null;
    }

}
