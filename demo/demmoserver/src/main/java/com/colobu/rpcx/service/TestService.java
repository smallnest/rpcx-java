package com.colobu.rpcx.service;

import com.colobu.rpcx.rpc.annotation.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 * @author goodjava@qq.com
 */
@Service
//@Provider(name = "TestService", version = "0.0.2", token = "zzy123", timeout = 1000, cache = true)
//@Provider(name = "TestService", version = "0.0.2", token = "zzy123", tps = 1)
//@Provider(name = "TestService", version = "0.0.2", weight = "service_weight", group = "test")
@Provider(name = "TestService", version = "0.0.2", weight = "service_weight")
public class TestService implements ITestService {

    private static final Logger logger = LoggerFactory.getLogger(TestService.class);

    private Random random = new Random();

    public String hi(String str) {

//        logger.info("-------------->call hi:{}", str);
//        int i = random.nextInt(6);
//        if (i != 5) {
//            logger.info("-------------->|||call error " + i);
//            throw new RuntimeException("call hi error!!!!");
//        }
//        logger.info("-------------->call success " + i);

//        try {
//            TimeUnit.SECONDS.sleep(15);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return "hi ####" + str;
    }


    public byte[] golangHi(byte[] data) {
        return ("hi " + new String(data)).getBytes();
    }

    @Provider(cache = true)
    public int sum(int a, int b) {
        return a + b;
    }

}
