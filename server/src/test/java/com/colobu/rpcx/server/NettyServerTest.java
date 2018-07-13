package com.colobu.rpcx.server;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhangzhiyong on 2018/7/4.
 */
public class NettyServerTest {


    @Test
    public void testNettyServer() throws InterruptedException {
        NettyServer server = new NettyServer();
        server.start();
        ServiceRegister reg = new ServiceRegister("/youpin/services/", server.getAddr() + ":" + server.getPort());
        reg.register();
        reg.start();
        TimeUnit.HOURS.sleep(1);
    }
}
