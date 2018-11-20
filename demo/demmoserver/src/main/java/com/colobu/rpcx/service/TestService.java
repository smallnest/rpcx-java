package com.colobu.rpcx.service;

import com.colobu.rpcx.rpc.annotation.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@Provider(name = "TestService", version = "0.0.2", weight = "rpc.service.weight")
public class TestService implements ITestService {

    private static final Logger logger = LoggerFactory.getLogger(TestService.class);


    @Autowired
    private ITest2Service test2Service;

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

    public String async() {
        return "result:" + System.currentTimeMillis();
    }


    public byte[] golangHi(byte[] data) {
        return ("hi " + new String(data)).getBytes();
    }

    public int sum(int a, int b) {
        return a + b;
    }

    /**
     * 再次调用其他服务
     * @param a
     * @param b
     * @return
     */
    public int sum2(int a, int b) {
        return a + b + test2Service.num();
    }

    /**
     * 测试错误信息
     *
     * @param a
     * @param b
     * @return
     */
    public int error(int a, int b) {
        if (1 == 1) {
            throw new RuntimeException("system error!!!!");
        }
        return a + b;
    }

}
