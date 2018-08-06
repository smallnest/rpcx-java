package com.colobu.rpcx.url;

import com.colobu.rpcx.rpc.URL;
import org.junit.Test;

public class URLTest {


    @Test
    public void testUrl() {
        URL url = new URL("rpcx","",0);
        url.setPath("com.test.Service");
        url = url.addParameter("weight",22);
        url = url.addParameter("name","zzy");
        System.out.println(url.toFullString());

        System.out.println(url.getParameter("weight"));
        System.out.println(url.getPath());

        System.out.println(url.toParameterString());


        URL url2 = URL.valueOf("123.0.0.1:8888?a=3&b=2");
        System.out.println(url2.getAddress());
    }
}
