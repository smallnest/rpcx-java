package com.colobu.rpcx.common;

/**
 * @author goodjava@qq.com
 */
public class RpcxVersion {

    private String version = "1.2.6";
    private String date = "20181102";


    @Override
    public String toString() {
        return version + " " + date;
    }
}
