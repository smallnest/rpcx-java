package com.colobu.rpcx.service;

/**
 * Created by zhangzhiyong on 2018/7/4.
 * 服务类
 */
public class Arith implements IArith {

    public byte[] Echo(byte[] params) {
        return (new String(params) + " java server").getBytes();
    }


    public Integer sum(Integer a, Integer b) {
        return a + b;
    }

    public int sum2(int a, int b) {
        return a + b;
    }

}
