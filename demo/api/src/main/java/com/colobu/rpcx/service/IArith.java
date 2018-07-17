package com.colobu.rpcx.service;

import com.colobu.rpcx.rpc.annotation.Consumer;

@Consumer(impl = "com.colobu.rpcx.service.Arith")
public interface IArith {

    Integer sum(Integer a, Integer b);

    int sum2(int a, int b);

    String hi(String name);
}
