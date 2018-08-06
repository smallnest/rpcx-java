package com.colobu.rpcx.demo;

import com.colobu.rpcx.register.IServiceRegister;
import com.colobu.rpcx.server.NettyServer;
import com.colobu.rpcx.server.ZkServiceRegister;


/**
 * @author goodjava@qq.com
 */
public class Server {

    public static void main(String... args) {
        NettyServer server = new NettyServer();
        server.start();
        IServiceRegister reg = new ZkServiceRegister("/youpin/services/", server.getAddr() + ":" + server.getPort(), "com.colobu");
        reg.register();
        reg.start();
        server.await();
    }
}
