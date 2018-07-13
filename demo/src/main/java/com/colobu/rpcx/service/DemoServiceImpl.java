package com.colobu.rpcx.service;


import com.colobu.rpcx.rpc.annotation.Provider;

@Provider
public class DemoServiceImpl implements DemoService {


    public String test(String str) {
        return "test" + "," + str;
    }
}
