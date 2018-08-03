package com.colobu.rpcx.rpc;

import org.junit.Test;

/**
 * Created by goodjava@qq.com.
 */
public class ProxyTest {



    @Test
    public void testCglib() {
        IA proxy = new CglibProxy().getProxy(IA.class,(m,args)->{
            return "dddd";
        });
        String res = proxy.call();
        System.out.println(res);

    }

}
