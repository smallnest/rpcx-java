package com.colobu.rpcx.common;

/**
 * @author goodjava@qq.com
 */
public class RpcxVersion {

    private String version = "0.0.2";
    private String date = "20181007";


    @Override
    public String toString() {
        return version + " " + date;
    }
}