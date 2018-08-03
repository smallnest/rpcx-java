package com.colobu.rpcx.service;


import com.colobu.rpcx.rpc.annotation.Consumer;

/**
 * @author goodjava@qq.com
 */
@Consumer(impl = "com.colobu.rpcx.service.DemoServiceImpl")
public interface DemoService {

    String test(String str);
}
