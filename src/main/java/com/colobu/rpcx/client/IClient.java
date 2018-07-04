package com.colobu.rpcx.client;

import com.colobu.rpcx.protocol.Message;

import java.io.IOException;

/**
 * Created by zhangzhiyong on 2018/7/3.
 */
public interface IClient {

    Message call(String addr,Message req) throws Exception;

    void connect(String serverAddress, int serverPort) throws IOException;
}
