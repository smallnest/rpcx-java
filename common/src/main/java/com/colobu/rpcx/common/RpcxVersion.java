package com.colobu.rpcx.common;

/**
 * @author goodjava@qq.com
 */
public class RpcxVersion {

    private String version = "1.2.0";
    private String date = "20181023";


    @Override
    public String toString() {
        return version + " " + date;
    }
}
