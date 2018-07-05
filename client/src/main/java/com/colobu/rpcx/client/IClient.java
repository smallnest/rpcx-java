package com.colobu.rpcx.client;

import com.colobu.rpcx.protocol.Message;

/**
 * Created by zhangzhiyong on 2018/7/3.
 */
public interface IClient {

    Message call(String addr, Message req) throws Exception;

    Message call(Message req) throws Exception;
}
