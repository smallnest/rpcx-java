package com.colobu.rpcx.filter;

import com.colobu.rpcx.filter.impl.EchoFilter;
import org.junit.Test;

public class FilterTest {


    @Test
    public void testName() {
        EchoFilter filter = new EchoFilter();
        System.out.println(filter.name());
    }
}
