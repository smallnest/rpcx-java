package com.colobu.rpcx.client.impl;

import com.colobu.rpcx.client.Selector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by zhangzhiyong on 2018/7/4.
 */
public class RandomSelector implements Selector {

    private static final Logger logger = LoggerFactory.getLogger(RandomSelector.class);

    @Override
    public String select(String servicePath, String serviceMethod, List<String> list) {
        if (list.size() > 0) {
            String addr = list.get(0);
            logger.info("---->random select addr:{}", addr);
            return addr;
        }
        return null;
    }
}
