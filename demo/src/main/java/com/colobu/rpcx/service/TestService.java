package com.colobu.rpcx.service;

import com.colobu.rpcx.rpc.annotation.Provider;
import org.springframework.stereotype.Service;

@Service
@Provider
public class TestService {


//    @Provider
    public String test() {
        return "test";
    }

}
