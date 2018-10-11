package com.colobu.rpcx.common;

/**
 * @author goodjava@qq.com
 */
public class RpcxVersion {

    private String version = "0.0.1";
    private String date = "20181011";


    @Override
    public String toString() {
        return version + " " + date;
    }
}
