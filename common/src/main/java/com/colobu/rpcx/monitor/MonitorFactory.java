package com.colobu.rpcx.monitor;


import com.colobu.rpcx.monitor.impl.RpcxMonitor;
import com.colobu.rpcx.rpc.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MonitorFactory {

    private static final Logger logger = LoggerFactory.getLogger(MonitorFactory.class);

    /**
     * Create monitor.
     */
    public Monitor getMonitor(URL url) {
        return new RpcxMonitor(new MonitorService() {
            @Override
            public void collect(URL statistics) {
                logger.info(statistics.toFullString());
            }

            @Override
            public List<URL> lookup(URL query) {
                return null;
            }
        });
    }

}