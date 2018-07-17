package com.colobu.rpcx.service;


import com.colobu.rpcx.rpc.annotation.Consumer;

@Consumer(impl = "com.colobu.rpcx.service.DemoServiceImpl")
public interface DemoService {

    String test(String str);
}
