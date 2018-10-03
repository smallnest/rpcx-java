package com.colobu.rpcx.rpc.test;

import com.colobu.rpcx.rpc.impl.Exporter;
import org.junit.Test;

public class ExporterTest {

    @Test
    public void testExport() {
        System.out.println(new Exporter(null).export("com.colobu.rpcx"));
    }
}
