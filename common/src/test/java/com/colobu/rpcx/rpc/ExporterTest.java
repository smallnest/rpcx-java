package com.colobu.rpcx.rpc;

import com.colobu.rpcx.rpc.impl.Exporter;
import org.junit.Test;

/**
 * Created by goodjava@qq.com.
 */
public class ExporterTest {


    @Test
    public void testExport() {
        System.out.println(new Exporter().export(""));
    }
}
