package com.colobu.rpcx.tps;


import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.rpc.Invocation;
import com.colobu.rpcx.rpc.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author goodjava@qq.com
 */
public class DefaultTPSLimiter implements TPSLimiter {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTPSLimiter.class);

    private final ConcurrentMap<String, StatItem> stats = new ConcurrentHashMap<>();

    @Override
    public boolean isAllowable(URL url, Invocation invocation) {
        int rate = url.getParameter(Constants.TPS_LIMIT_RATE_KEY, -1);
        //默认时间间隔是60秒
        long interval = url.getParameter(Constants.TPS_LIMIT_INTERVAL_KEY, Constants.DEFAULT_TPS_LIMIT_INTERVAL);
        String serviceKey = url.getServiceKey();
        logger.debug("DefaultTPSLimiter interval:{} serviceKey:{}",interval,serviceKey);
        if (rate > 0) {
            StatItem statItem = stats.get(serviceKey);
            if (statItem == null) {
                stats.putIfAbsent(serviceKey,
                        new StatItem(serviceKey, rate, interval));
                statItem = stats.get(serviceKey);
            }
            return statItem.isAllowable();
        } else {//如果不开启,删除之前记录的即可
            StatItem statItem = stats.get(serviceKey);
            if (statItem != null) {
                stats.remove(serviceKey);
            }
        }

        return true;
    }

}
