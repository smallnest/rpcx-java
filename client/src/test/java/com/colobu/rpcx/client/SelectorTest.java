package com.colobu.rpcx.client;

import com.colobu.rpcx.client.impl.RandomSelector;
import org.junit.Test;

/**
 * Created by zhangzhiyong on 2018/7/4.
 */
public class SelectorTest {


    @Test
    public void testRandomSelector() {
        System.out.println(new RandomSelector().select("","", null));
    }
}
