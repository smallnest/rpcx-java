package com.colobu.rpcx.server;

import com.colobu.rpcx.rpc.impl.Exporter;
import com.colobu.rpcx.utils.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangzhiyong on 2018/7/4.
 */
public class ZkServiceRegister implements IServiceRegister {

    private static final Logger logger = LoggerFactory.getLogger(ZkServiceRegister.class);

    private String basePath;
    private Set<String> serviceName;
    private String addr;

    public ZkServiceRegister(String basePath, String addr, String providerPackage) {
        this.basePath = basePath;
        this.addr = addr;

        logger.info("export service names:{}", this.serviceName);
        this.serviceName = new Exporter().export(providerPackage);//导出所有有注解的service
    }

    //服务注册
    @Override
    public void register() {
        try {
            ZkClient.ins().create(this.basePath, this.serviceName, this.addr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //周期性的注册
    @Override
    public void start() {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            register();
        }, 0, 5, TimeUnit.SECONDS);
    }

}
