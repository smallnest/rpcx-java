package com.colobu.rpcx.utils;

import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangzhiyong on 2018/7/4.
 */
public class ZkClientTest {


    @Test
    public void testWatch() throws Exception {
        ZkClient.ins().watch();

        LinkedBlockingQueue<PathStatus> queue = new LinkedBlockingQueue<>();
        new Thread(()->{
           while(true) {
               try {
                   PathStatus ps = queue.take();
                   System.out.println("---------->"+ps);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
        }).start();

        ZkClient.ins().watch2(queue,"");
        TimeUnit.HOURS.sleep(1);
    }


    @Test
    public void testCreate() throws Exception {
        ZkClient.ins().create("","","");
        TimeUnit.HOURS.sleep(1);
    }
}
