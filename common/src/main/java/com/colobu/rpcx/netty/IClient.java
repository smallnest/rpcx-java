package com.colobu.rpcx.netty;

import com.colobu.rpcx.protocol.Message;

/**
 * @author goodjava@qq.com
 */
public interface IClient {

    /**
     * 直连访问
     * @param addr
     * @param req
     * @return
     * @throws Exception
     */
    Message call(String addr, Message req) throws Exception;

    /**
     * 会自动选择要访问的服务器
     * @param req
     * @param timeOut
     * @return
     * @throws Exception
     */
    Message call(Message req, long timeOut) throws Exception;

    /***
     *指定超时时间
     */
    Message call(String addr, Message req, long timeOut) throws Exception;

}


