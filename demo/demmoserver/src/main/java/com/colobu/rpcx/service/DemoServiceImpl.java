package com.colobu.rpcx.service;


import com.colobu.rpcx.rpc.annotation.Provider;
import org.springframework.stereotype.Service;

/**
 * @author goodjava@qq.com
 */
@Service
@Provider
public class DemoServiceImpl implements DemoService {


    public String test(String str) {
        return "test" + "," + str;
    }
}
