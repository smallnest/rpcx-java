package com.colobu.rpcx.demo;

import com.colobu.rpcx.client.NettyClient;
import com.colobu.rpcx.client.ServiceDiscovery;
import com.colobu.rpcx.netty.IClient;
import com.colobu.rpcx.rpc.impl.Consumer;
import com.colobu.rpcx.service.IArith;


public class Client {


    public static void main(String... args) {
        ServiceDiscovery serviceDiscovery = new ServiceDiscovery("/youpin/services/", "com.colobu.rpcx.service.Arith");
        IClient client = new NettyClient(serviceDiscovery);
        IArith arith = new Consumer(client).refer(IArith.class);
//        int result = arith.sum(1111, 222);
//        int result = arith.sum2(1111, 222);
        for (int i = 0; i < 1; i++) {
            String result = arith.hi("zzy");
            System.out.println("------------->" + result);
        }
    }
}
