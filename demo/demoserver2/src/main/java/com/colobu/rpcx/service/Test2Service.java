package com.colobu.rpcx.service;

import com.colobu.rpcx.rpc.annotation.Provider;
import com.colobu.rpcx.service.ITest2Service;
import org.springframework.stereotype.Service;

/**
 * @author goodjava@qq.com
 */
@Service
@Provider(name = "Test2Service")
public class Test2Service implements ITest2Service {

    public int num() {
        return 100;
    }
}
