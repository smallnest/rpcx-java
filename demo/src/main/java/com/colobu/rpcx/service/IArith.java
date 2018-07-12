package com.colobu.rpcx.service;

import com.colobu.rpcx.rpc.annotation.Provider;

@Provider(impl = "com.colobu.rpcx.service.Arith")
public interface IArith {

    Integer sum(Integer a, Integer b);

    int sum2(int a, int b);
}
