package com.colobu.rpcx.client;

import com.colobu.rpcx.client.http.RpcxHttpClient;
import com.colobu.rpcx.common.Pair;
import org.junit.Test;

public class RpcxHttpClientTest {


    @Test
    public void testExecute() {
        String res = RpcxHttpClient.execute("http://192.168.31.82:8031/", "com.colobu.rpcx.service.TestService", "sum", Pair.of("int", 11), Pair.of("int", 22));
        System.out.println(res);
    }
}
