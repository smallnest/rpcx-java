package com.colobu.rpcx.common;

import org.junit.Test;

/**
 * Created by zhangzhiyong on 2018/7/5.
 */
public class CommonTest {


    @Test
    public void testSwitch() {
        int i = 12;
        switch (i) {
            case 12:
                System.out.println(12);
            case 13:
                System.out.println(13);
        }
    }
}
