package com.colobu.rpcx.service;

import com.colobu.rpcx.rpc.annotation.Provider;
import org.springframework.stereotype.Service;

/**
 * @author goodjava@qq.com
 */
@Provider
@Service
public class Arith implements IArith {

    /**
     * golang 调用
     */
    public byte[] Echo(byte[] params) {
        return (new String(params) + " java server!!!").getBytes();
    }

    public Integer sum(Integer a, Integer b) {
        return a + b;
    }

    public int sum2(int a, int b) {
        return a + b;
    }


}
