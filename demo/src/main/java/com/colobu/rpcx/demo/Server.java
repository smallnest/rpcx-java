package com.colobu.rpcx.demo;

import com.colobu.rpcx.server.IServiceRegister;
import com.colobu.rpcx.server.NettyServer;
import com.colobu.rpcx.server.ZkServiceRegister;


/**
 * Created by zhangzhiyong on 2018/7/5.
 * 启动server
 */
public class Server {

    public static void main(String... args) {
        NettyServer server = new NettyServer();
        server.start();
        IServiceRegister reg = new ZkServiceRegister("/youpin/services/", server.getAddr() + ":" + server.getPort());
        reg.register();
        reg.start();
        server.await();
    }
}
