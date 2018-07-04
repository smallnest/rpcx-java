package com.colobu.rpcx.server;

import com.colobu.rpcx.utils.ZkClient;

/**
 * Created by zhangzhiyong on 2018/7/4.
 */
public class ServiceRegister {

    public void register() {
        try {
            ZkClient.ins().create();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
