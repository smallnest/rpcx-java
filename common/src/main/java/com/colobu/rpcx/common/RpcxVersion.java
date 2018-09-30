package com.colobu.rpcx.common;

/**
 * @author goodjava@qq.com
 */
public class RpcxVersion {

    private String version = "0.1.4";
    private String date = "20180930";


    @Override
    public String toString() {
        return version + " " + date;
    }
}
