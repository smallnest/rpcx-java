package com.colobu.rpcx.service;

/**
 * Created by zhangzhiyong on 2018/7/4.
 * 服务类
 */
public class Arith {

    public byte[] Echo(byte[] params) {
        return (new String(params)+" java server") .getBytes();
    }

}
