package com.colobu.rpcx.netty;

import com.colobu.rpcx.protocol.Message;

/**
 * Created by zhangzhiyong on 2018/7/3.
 */
public interface IClient {

    //直连访问
    Message call(String addr, Message req) throws Exception;

    Message call(Message req) throws Exception;

    //指定超时时间
    Message call(String addr, Message req, long timeOut) throws Exception;

}


