package com.colobu.rpcx.server;

import com.colobu.rpcx.register.IServiceRegister;
import com.colobu.rpcx.rpc.impl.Exporter;
import com.colobu.rpcx.utils.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author goodjava@qq.com
 */
public class ZkServiceRegister implements IServiceRegister {

    private static final Logger logger = LoggerFactory.getLogger(ZkServiceRegister.class);

    private String basePath;
    private Set<String> serviceNameSet;
    private String addr;

    public ZkServiceRegister(String basePath, String addr, String providerPackage, Function<Class, Object> getBeanFunc) {
        this.basePath = basePath;
        this.addr = addr;
        //导出所有有注解的service
        this.serviceNameSet = new Exporter(getBeanFunc, addr).export(providerPackage);
        logger.info("export service names:{}", this.serviceNameSet);
    }

    /**
     * 服务注册
     */
    @Override
    public void register() {
        try {
            ZkClient.ins().create(this.basePath, this.serviceNameSet, this.addr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 周期性的注册
     */
    @Override
    public void start() {
        new ScheduledThreadPoolExecutor(1).scheduleWithFixedDelay(() -> {
            register();
        }, 0, 5, TimeUnit.SECONDS);
    }

}
