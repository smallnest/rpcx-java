package com.colobu.rpcx.demo;

import com.colobu.rpcx.rpc.impl.Consumer;
import com.colobu.rpcx.service.IArith;

public class Client {


    public static void main(String... args) {
        IArith arith = new Consumer().refer(IArith.class);
//        int result = arith.sum(1111, 222);
        int result = arith.sum2(1111, 222);
        System.out.println(result);
    }
}
