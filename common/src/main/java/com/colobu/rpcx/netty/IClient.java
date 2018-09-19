package com.colobu.rpcx.netty;

import com.colobu.rpcx.discovery.IServiceDiscovery;
import com.colobu.rpcx.protocol.Message;

/**
 * @author goodjava@qq.com
 */
public interface IClient {

    Message call(Message req, long timeOut) throws Exception;

    Message call(String addr, Message req, long timeOut) throws Exception;

    IServiceDiscovery getServiceDiscovery();

    void close();

}


