package com.colobu.rpcx.demo;

import com.colobu.rpcx.discovery.IServiceDiscovery;
import com.colobu.rpcx.client.NettyClient;
import com.colobu.rpcx.client.ZkServiceDiscovery;
import com.colobu.rpcx.netty.IClient;
import com.colobu.rpcx.rpc.impl.ConsumerConfig;
import com.colobu.rpcx.service.IArith;


/**
 * @author goodjava@qq.com
 */
public class Client {

    public static void main(String... args) {
        IServiceDiscovery serviceDiscovery = new ZkServiceDiscovery("/youpin/services/","");
        IClient client = new NettyClient(serviceDiscovery);
        IArith arith = new ConsumerConfig(client).refer(IArith.class);
////        System.out.println(arith.sum(1111, 222));
////        System.out.println(arith.sum2(1111, 222));
        for (int i = 0; i < 1; i++) {
            System.out.println(arith.sum(11,22));
        }
//
        serviceDiscovery.close();
        System.out.println("client call finish");
    }
}
