package com.colobu.rpcx.service;

/**
 * Created by zhangzhiyong on 2018/7/4.
 */
public class Arith {

    public byte[] Echo(byte[] params) {
        return ("java:" + new String(params)).getBytes();
    }

}
