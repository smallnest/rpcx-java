package com.colobu.rpcx.common;

import com.colobu.rpcx.selector.Weighted;
import com.colobu.rpcx.selector.WeightedRountRobin;
import org.junit.Test;

public class WeightedRountRobinTest {


    @Test
    public void testWeightedRountRobin() {
//        w := &W1{}
//        w.Add("a", 5)
//        w.Add("b", 2)
//        w.Add("c", 3)
//        for i := 0; i < 10; i++ {
//            fmt.Printf("%s ", w.Next())
//        }

        Weighted[] array = new Weighted[3];
        array[0] = new Weighted("a", 3);
        array[1] = new Weighted("b", 3);
        array[2] = new Weighted("c", 4);

        WeightedRountRobin weightedRountRobin = new WeightedRountRobin(array);

        for (int i = 0; i < 10; i++) {
            Weighted a = weightedRountRobin.nextWeighted();
            System.out.println(a.getServer());
        }
    }
}
