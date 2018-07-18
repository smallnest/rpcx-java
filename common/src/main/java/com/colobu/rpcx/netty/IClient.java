package com.colobu.rpcx.netty;

import com.colobu.rpcx.protocol.Message;

/**
 * Created by zhangzhiyong on 2018/7/3.
 */
public interface IClient {

    //直连访问
    Message call(String addr, Message req) throws Exception;

    //会自动选择要访问的服务器
    Message call(Message req, long timeOut) throws Exception;

    //指定超时时间
    Message call(String addr, Message req, long timeOut) throws Exception;

}


