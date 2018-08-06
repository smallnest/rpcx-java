package com.colobu.rpcx.common;

import com.colobu.rpcx.selector.Weighted;
import com.colobu.rpcx.selector.WeightedRountRobin;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class WeightedRountRobinTest {


    @Test
    public void testWeightedRountRobin() {
        Weighted[] array = new Weighted[3];
        array[0] = new Weighted("a", 5);
        array[1] = new Weighted("b", 1);
        array[2] = new Weighted("c", 1);

        WeightedRountRobin weightedRountRobin = new WeightedRountRobin(array);


        weightedRountRobin.updateWeighteds(Arrays.asList());

        for (int i = 0; i < 7; i++) {
            Weighted a = weightedRountRobin.nextWeighted();
            System.out.println(a.getServer());
        }
    }


    @Test
    public void testUpdateWeightedRountRobin() {

        List<String> serviceList = Arrays.asList("127.0.0.1:8001?weight=10");
        WeightedRountRobin weightedRountRobin = new WeightedRountRobin(serviceList);
        weightedRountRobin.updateWeighteds(Arrays.asList("127.0.0.1:8002?weight=10","127.0.0.1:8001?weight=9"));

    }
}
