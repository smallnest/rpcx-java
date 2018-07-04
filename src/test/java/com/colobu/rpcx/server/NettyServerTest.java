package com.colobu.rpcx.server;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhangzhiyong on 2018/7/4.
 */
public class NettyServerTest {


    @Test
    public void testNettyServer() throws InterruptedException {
        new ServiceRegister().register();
        NettyServer server = new NettyServer();
        server.start();
        TimeUnit.HOURS.sleep(1);
    }
}
