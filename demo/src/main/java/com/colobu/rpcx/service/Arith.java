package com.colobu.rpcx.service;

import com.colobu.rpcx.rpc.annotation.Provider;

/**
 * Created by zhangzhiyong on 2018/7/4.
 * 服务类
 */
@Provider
public class Arith implements IArith {

    /**
     * golang 调用
     *
     * @param params
     * @return
     */
    public byte[] Echo(byte[] params) {
        return (new String(params) + " java server").getBytes();
    }


    public Integer sum(Integer a, Integer b) {
        return a + b;
    }

    public int sum2(int a, int b) {
        System.out.println("----------->sum2");
        return a + b;
    }

    public String hi(String name) {
        return "hi " + name;
    }

}
