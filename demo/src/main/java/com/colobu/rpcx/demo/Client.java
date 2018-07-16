package com.colobu.rpcx.demo;

import com.colobu.rpcx.client.IServiceDiscovery;
import com.colobu.rpcx.client.NettyClient;
import com.colobu.rpcx.client.ZkServiceDiscovery;
import com.colobu.rpcx.netty.IClient;
import com.colobu.rpcx.rpc.impl.ConsumerConfig;
import com.colobu.rpcx.service.IArith;


public class Client {


    public static void main(String... args) {
        //服务发现者
        IServiceDiscovery serviceDiscovery = new ZkServiceDiscovery("/youpin/services/");
        IClient client = new NettyClient(serviceDiscovery);
        IArith arith = new ConsumerConfig(client).refer(IArith.class);
//        int result = arith.sum(1111, 222);
//        int result = arith.sum2(1111, 222);
        for (int i = 0; i < 1; i++) {
            String result = arith.hi("zzy");
            System.out.println("------------->" + result);
        }
    }
}
