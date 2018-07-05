package com.colobu.rpcx.demo;

import com.colobu.rpcx.server.NettyServer;
import com.colobu.rpcx.server.ServiceRegister;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhangzhiyong on 2018/7/5.
 */
public class Bootstrap {

    public static void main(String... args) throws InterruptedException {
        NettyServer server = new NettyServer();
        server.start();
        ServiceRegister reg = new ServiceRegister("/youpin/services/", "Arith", server.getAddr() + ":" + server.getPort());
        reg.register();
        reg.start();
        TimeUnit.HOURS.sleep(1);

    }
}
