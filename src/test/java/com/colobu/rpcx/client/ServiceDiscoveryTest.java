package com.colobu.rpcx.client;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhangzhiyong on 2018/7/4.
 */
public class ServiceDiscoveryTest {


    @Test
    public void testWatch() throws Exception {
        new ServiceDiscovery().watch();
        TimeUnit.HOURS.sleep(1);
    }
}
