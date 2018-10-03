package com.colobu.rpcx.server;

import org.junit.Test;

import java.util.concurrent.TimeUnit;


/**
 * Created by goodjava@qq.com.
 */
public class NettyServerTest {


    @Test
    public void testNettyServer() throws InterruptedException {
        NettyServer server = new NettyServer();
        server.start();
        ZkServiceRegister reg = new ZkServiceRegister("/youpin/services/", server.getAddr() + ":" + server.getPort(), "", null);
        reg.register();
        reg.start();
        TimeUnit.HOURS.sleep(1);
    }
}
