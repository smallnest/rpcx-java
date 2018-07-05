package com.colobu.rpcx.server;

import com.colobu.rpcx.utils.ZkClient;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangzhiyong on 2018/7/4.
 */
public class ServiceRegister {

    private String basePath;
    private String serviceName;
    private String addr;

    public ServiceRegister(String basePath, String serviceName, String addr) {
        this.basePath = basePath;
        this.serviceName = serviceName;
        this.addr = addr;
    }

    //服务注册
    public void register() {
        try {
            ZkClient.ins().create(this.basePath,this.serviceName,this.addr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            register();
        }, 0, 5, TimeUnit.SECONDS);
    }

}
