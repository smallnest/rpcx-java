package com.colobu.rpcx.service;

import com.colobu.rpcx.rpc.annotation.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


/**
 * @author goodjava@qq.com
 */
@Service
//@Provider(name = "TestService", version = "0.0.2", token = "zzy123", timeout = 1000, cache = true)
//@Provider(name = "TestService", version = "0.0.2", token = "zzy123", tps = 1)
@Provider(name = "TestService", version = "0.0.2")
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

    //不会调过来(echo 的会呗echo filter 拦截下来)
    public String $echo(String str) {
        return null;
    }

    public byte[] golangHi(byte[] data) {
        return ("hi " + new String(data)).getBytes();
    }

    public int sum(int a, int b) {
        return a + b;
    }

}
