package com.colobu.rpcx.common;

import com.colobu.rpcx.common.retry.RetryNTimes;
import com.colobu.rpcx.common.retry.RetryPolicy;
import com.colobu.rpcx.rpc.A;
import com.colobu.rpcx.rpc.HessianUtils;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangzhiyong on 2018/7/5.
 */
public class CommonTest {


    @Test
    public void testRetry() {
        RetryPolicy retryPolicy = new RetryNTimes(10);
        retryPolicy.retry((i)->{
            System.out.println(i);
            if (i == 3) {
                return true;
            }
            return false;
        });
    }


    @Test
    public void testConfig() {
        System.out.println(Config.ins().get("server.type"));
    }


    @Test
    public void testClassPathResource()  {
        System.out.println(new ClassPathResource("test").getString());
        System.out.println(new ClassPathResource("test").getProperties().getProperty("2"));
    }


    class E {
        public E() {
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(()->{
                System.out.println("schedule");
            },10,1,TimeUnit.SECONDS);

            Executors.newSingleThreadScheduledExecutor()
                .scheduleWithFixedDelay(() -> {
                    System.out.println("schedule");
        }, 0, 5, TimeUnit.SECONDS);
        }
    }


    @Test
    public void testSchedule() {
        E e = new E();
        System.out.println(e);
        System.out.println("finish");
    }


    @Test
    public void testSwitch() {
        int i = 12;
        switch (i) {
            case 12:
                System.out.println(12);
            case 13:
                System.out.println(13);
        }
    }


    @Test
    public void testLocalAddr() {
        System.out.println(RemotingUtil.getLocalAddress());
    }


    @Test
    public void testClazz() {
        int i = 12;
        System.out.println(new Object[]{i}[0].getClass().getTypeName());
    }


    @Test
    public void testGson() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        RpcInvocation invocation = new RpcInvocation();
        invocation.setClassName("name");
        String data = gson.toJson(invocation);
        System.out.println(data);
    }


    @Test
    public void testHessian() {
        Object[] obj = new Object[]{1, 2, 3, 4, "a", 1.0, new A()};
        byte[] data = HessianUtils.write(obj);
        System.out.println(Arrays.toString(data));

        Object[] obj2 = (Object[]) HessianUtils.read(data);
        System.out.println(Arrays.toString(obj2));

    }

}
