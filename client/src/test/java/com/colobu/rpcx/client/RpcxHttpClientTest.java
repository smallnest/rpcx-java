package com.colobu.rpcx.client;

import com.colobu.rpcx.client.http.RpcxHttpClient;
import com.colobu.rpcx.common.Pair;
import org.junit.Test;

public class RpcxHttpClientTest {


    @Test
    public void testExecute() {
        String res = RpcxHttpClient.execute("http://10.231.72.140:8033/", "com.colobu.rpcx.service.TestService", "sum", Pair.of("int", "11"), Pair.of("int", "22"));
        System.out.println(res);
    }
}
