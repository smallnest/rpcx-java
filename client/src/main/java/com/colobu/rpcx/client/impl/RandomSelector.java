package com.colobu.rpcx.client.impl;

import com.colobu.rpcx.client.Selector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 * Created by zhangzhiyong on 2018/7/4.
 */
public class RandomSelector implements Selector {

    private static final Logger logger = LoggerFactory.getLogger(RandomSelector.class);

    @Override
    public String select(String servicePath, String serviceMethod, List<String> list) {
        if (list.size() > 0) {
            Random random = new Random();
            int i = random.nextInt(list.size());
            String addr = list.get(i);
            logger.info("random select addr:{}", addr);
            return addr;
        }
        return null;
    }
}
